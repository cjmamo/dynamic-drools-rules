package org.ossandme;

public class AlertDecision {
    private Boolean doAlert;

    public AlertDecision() {
        this.doAlert = false;
    }

    public Boolean getDoAlert() {
        return doAlert;
    }

    public void setDoAlert(Boolean doAlert) {
        this.doAlert = doAlert;
    }
}
