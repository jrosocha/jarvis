package com.jhr.jarvis.controllers;

import javax.annotation.PostConstruct;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextArea;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationListener;

import com.jhr.jarvis.event.ConsoleEvent;

/**
 * Not annotated as @Component because we want to use the springFxmlLoader so the @FXML annotations get populated.
 * Wired in @ JarvisConfig.
 * @author jrosocha
 *
 */
public class ConsoleController implements ApplicationListener<ConsoleEvent> {
    
    @FXML
    private Node view;
    
    @FXML
    private TextArea console;
    
    @PostConstruct
    public void initConsole() {

    }
    
    @Override
    public void onApplicationEvent(ConsoleEvent event) {
        
        Platform.runLater(()->{
            if (StringUtils.isNotBlank(event.getMessage())) {
                try {
                    synchronized(console) {
                        System.out.println("console " + console.lengthProperty());
                        System.out.println("trying to append " +event.getMessage());
                        if (console.getText().length() > 50000) {
                            console.setText(console.getText(25000, console.getText().length()));
                        }
                        console.appendText(event.getMessage() + "\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    public Node getView() {
        return view;
    }

}
