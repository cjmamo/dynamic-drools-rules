package org.ossandme;

import org.drools.template.ObjectDataCompiler;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.ossandme.rule.Condition;
import org.ossandme.rule.Rule;
import org.ossandme.event.Event;
import org.ossandme.event.OrderEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class Program {

    static public void main(String[] args) throws Exception {

        // Create an event that will be tested against the rule. In reality, the event would be read from some external store.
        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setPrice(5000.1);
        orderEvent.setCustomer("Widgets Inc.");

        Rule highValueOrderWidgetsIncRule = new Rule();

        Condition highValueOrderCondition = new Condition();
        highValueOrderCondition.setField("price");
        highValueOrderCondition.setOperator(Condition.Operator.GREATER_THAN);
        highValueOrderCondition.setValue(5000.0);

        Condition widgetsIncCustomerCondition = new Condition();
        widgetsIncCustomerCondition.setField("customer");
        widgetsIncCustomerCondition.setOperator(Condition.Operator.EQUAL_TO);
        widgetsIncCustomerCondition.setValue("Widgets Inc.");

        // In reality, you would have multiple rules for different types of events.
        // The eventType property would be used to find rules relevant to the event
        highValueOrderWidgetsIncRule.setEventType(Rule.eventType.ORDER);

        highValueOrderWidgetsIncRule.setConditions(Arrays.asList(highValueOrderCondition, widgetsIncCustomerCondition));

        String drl = applyRuleTemplate(orderEvent, highValueOrderWidgetsIncRule);
        AlertDecision alertDecision = evaluate(drl, orderEvent);

        System.out.println(alertDecision.getDoAlert());

        // doAlert is false by default
        if (alertDecision.getDoAlert()) {
            // do notification
        }

    }

    static private AlertDecision evaluate(String drl, Event event) throws Exception {
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.write("src/main/resources/rule.drl", drl);
        kieServices.newKieBuilder(kieFileSystem).buildAll();

        KieContainer kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
        StatelessKieSession statelessKieSession = kieContainer.getKieBase().newStatelessKieSession();

        AlertDecision alertDecision = new AlertDecision();
        statelessKieSession.getGlobals().set("alertDecision", alertDecision);
        statelessKieSession.execute(event);

        return alertDecision;
    }

    static private String applyRuleTemplate(Event event, Rule rule) throws Exception {
        Map<String, Object> data = new HashMap<String, Object>();
        ObjectDataCompiler objectDataCompiler = new ObjectDataCompiler();

        data.put("rule", rule);
        data.put("eventType", event.getClass().getName());

        return objectDataCompiler.compile(Arrays.asList(data), Thread.currentThread().getContextClassLoader().getResourceAsStream("rule-template.drl"));
    }
}
