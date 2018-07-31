package edu.learn.cdi.demo.bean;


import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;

@Named
@ApplicationScoped
public class ServiceBean implements Serializable {

    @PostConstruct
    public void init() {
        System.out.println("ServiceBean->Cdi bean");
    }

    //Will be called when @SessionScoped
    @PreDestroy
    public void destroying() {
        System.out.println("ServiceBean->Cdi bean");
    }



    public int doWork(int a, int b) {
        return a + b;
    }
}