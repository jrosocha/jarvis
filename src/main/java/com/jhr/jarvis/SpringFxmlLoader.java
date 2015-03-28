package com.jhr.jarvis;

import java.io.IOException;
import java.io.InputStream;

import javafx.fxml.FXMLLoader;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service
public class SpringFxmlLoader implements ApplicationContextAware {

    private ApplicationContext context;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;

    }   
    
    public Object load(String url, Class<?> controllerClass) throws IOException {
        try (InputStream fxmlStream = controllerClass.getResourceAsStream(url);) {
            Object instance = context.getBean(controllerClass);
            FXMLLoader loader = new FXMLLoader();
            loader.getNamespace().put("controller", instance);
            return loader.load(fxmlStream);
        }
    }
    
    public Object loadController(String url) throws IOException {
        try (InputStream fxmlStream = getClass().getResourceAsStream(url);) {        
            FXMLLoader loader = new FXMLLoader();
            loader.load(fxmlStream);
            return loader.getController();
        }
    }
   
}
