package de.ipbhalle.MetFlow.web.controller;

import javax.faces.event.ValueChangeEvent;

import com.icesoft.faces.context.effects.Effect;
import com.icesoft.faces.context.effects.Highlight;

public class DataExporter {
    private Effect changeEffect;
    private String type;

    public DataExporter() {
        changeEffect = new Highlight("#fda505");
        changeEffect.setFired(true);
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Effect getChangeEffect() {
        return changeEffect;
    }

    public void setChangeEffect(Effect changeEffect) {
        this.changeEffect = changeEffect;
    }
    
    public void typeChangeListener(ValueChangeEvent event){
        this.changeEffect.setFired(false);
    }
}
