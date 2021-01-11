package com.wso2telco.util;

public class TimeOutCount {

    private int variable = 0;
    private long variableUpdateMillis = Long.MAX_VALUE;

    public void setVariable(int value){
        if(this.variable==0)
            this.variableUpdateMillis = System.currentTimeMillis();
        this.variable = value;
    }

    public int getVariable(){
        if (System.currentTimeMillis() - this.variableUpdateMillis >= Long.parseLong(PropertyReader.getKafkaProperties()
                .get(Properties.VARIABLE_FRESHNESS_THRESHOLD))) {
            this.variable = 0;
        }
        return this.variable;
    }
}

