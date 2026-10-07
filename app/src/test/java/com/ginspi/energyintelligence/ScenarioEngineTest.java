package com.ginspi.energyintelligence;
import org.junit.Test; import static org.junit.Assert.assertTrue;
public class ScenarioEngineTest {
 @Test public void gasIncreaseDoesNotReducePricePressure(){ScenarioEngine.ScenarioResult base=ScenarioEngine.evaluate(new ScenarioEngine.ScenarioInput("Greece",0,0,0,0,0,0,0)); ScenarioEngine.ScenarioResult higher=ScenarioEngine.evaluate(new ScenarioEngine.ScenarioInput("Greece",30,0,0,0,0,0,0)); assertTrue(higher.priceImpactPct>=base.priceImpactPct);} 
 @Test public void riskScoreWithinBounds(){ScenarioEngine.ScenarioResult r=ScenarioEngine.evaluate(new ScenarioEngine.ScenarioInput("Germany",100,30,50,50,10,-50,100)); assertTrue(r.riskScore>=0&&r.riskScore<=100);} 
 @Test public void valuesClamped(){ScenarioEngine.ScenarioResult r=ScenarioEngine.evaluate(new ScenarioEngine.ScenarioInput("Italy",999,-999,999,-999,999,-999,999)); assertTrue(r.riskScore>=0&&r.riskScore<=100);} }
