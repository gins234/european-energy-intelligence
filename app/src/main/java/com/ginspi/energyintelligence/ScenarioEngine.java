package com.ginspi.energyintelligence;

public final class ScenarioEngine {
    private ScenarioEngine() {}

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static ScenarioInput sanitize(ScenarioInput input) {
        return new ScenarioInput(
                input.country,
                clamp(input.gas, -50, 100),
                clamp(input.demand, -20, 30),
                clamp(input.wind, -50, 50),
                clamp(input.solar, -50, 50),
                clamp(input.temp, -10, 10),
                clamp(input.imports, -50, 50),
                clamp(input.geo, 0, 100)
        );
    }

    public static ScenarioResult evaluate(ScenarioInput raw) {
        ScenarioInput input = sanitize(raw);
        CountryProfile profile = CountryProfiles.forCountry(input.country);

        double gasPressure = 0.42 * input.gas * profile.gasSensitivity;
        double demandPressure = 0.55 * input.demand;
        double windRelief = -0.20 * input.wind;
        double solarRelief = -0.14 * input.solar;
        double tempPressure = -0.35 * input.temp * profile.tempSensitivity;
        double importRelief = -0.18 * input.imports;
        double geoPressure = 0.22 * input.geo;

        double priceImpact = gasPressure + demandPressure + windRelief + solarRelief + tempPressure + importRelief + geoPressure;
        double demandImpact = input.demand + (-0.12 * input.temp) + (0.04 * input.geo / 10.0);
        double ren = clamp(profile.renewables + (0.30 * input.wind) + (0.22 * input.solar), 0, 100);
        double stress = clamp(25 + Math.abs(priceImpact) * 0.8 + Math.max(0, input.gas) * 0.20 + input.geo * 0.35 - input.imports * 0.15, 0, 100);
        double risk = clamp(0.4 * stress + 0.25 * Math.abs(priceImpact) + 0.2 * profile.baseRisk + 0.15 * input.geo, 0, 100);

        return new ScenarioResult(round1(priceImpact), round1(demandImpact), round1(risk), round1(ren), round1(stress));
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    public static final class ScenarioInput {
        public final String country;
        public final double gas;
        public final double demand;
        public final double wind;
        public final double solar;
        public final double temp;
        public final double imports;
        public final double geo;

        public ScenarioInput(String country, double gas, double demand, double wind, double solar, double temp, double imports, double geo) {
            this.country = country;
            this.gas = gas;
            this.demand = demand;
            this.wind = wind;
            this.solar = solar;
            this.temp = temp;
            this.imports = imports;
            this.geo = geo;
        }
    }

    public static final class ScenarioResult {
        public final double priceImpactPct;
        public final double demandImpactPct;
        public final double riskScore;
        public final double renewablesContributionPct;
        public final double marketStressLevel;

        public ScenarioResult(double priceImpactPct, double demandImpactPct, double riskScore, double renewablesContributionPct, double marketStressLevel) {
            this.priceImpactPct = priceImpactPct;
            this.demandImpactPct = demandImpactPct;
            this.riskScore = riskScore;
            this.renewablesContributionPct = renewablesContributionPct;
            this.marketStressLevel = marketStressLevel;
        }
    }

    private static final class CountryProfile {
        final double gasSensitivity;
        final double tempSensitivity;
        final double renewables;
        final double baseRisk;

        CountryProfile(double gasSensitivity, double tempSensitivity, double renewables, double baseRisk) {
            this.gasSensitivity = gasSensitivity;
            this.tempSensitivity = tempSensitivity;
            this.renewables = renewables;
            this.baseRisk = baseRisk;
        }
    }

    private static final class CountryProfiles {
        static CountryProfile forCountry(String country) {
            if (country == null) return defaults();
            switch (country) {
                case "Greece": return new CountryProfile(1.15, 0.90, 39, 58);
                case "Germany": return new CountryProfile(1.00, 0.78, 56, 42);
                case "France": return new CountryProfile(0.72, 0.62, 54, 30);
                case "Italy": return new CountryProfile(1.08, 0.84, 41, 49);
                case "Spain": return new CountryProfile(0.88, 0.74, 58, 35);
                case "Netherlands": return new CountryProfile(0.95, 0.70, 46, 38);
                case "Belgium": return new CountryProfile(0.92, 0.68, 49, 36);
                case "Austria": return new CountryProfile(0.74, 0.60, 67, 24);
                case "Portugal": return new CountryProfile(0.83, 0.73, 63, 28);
                case "Poland": return new CountryProfile(1.18, 0.80, 33, 61);
                default: return defaults();
            }
        }

        static CountryProfile defaults() {
            return new CountryProfile(1.0, 0.7, 45, 40);
        }
    }
}
