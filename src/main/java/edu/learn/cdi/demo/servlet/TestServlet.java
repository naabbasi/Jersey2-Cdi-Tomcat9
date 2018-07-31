package edu.learn.cdi.demo.servlet;

import edu.learn.cdi.demo.bean.ServiceBean;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "testServlet", urlPatterns = {"/testcdi"})
public class TestServlet extends HttpServlet {

  private static final long serialVersionUID = 2638127270022516617L;
  
  @Inject
  private ServiceBean service;
  
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int a = 3;
        int b = 3;

        PrintWriter out = response.getWriter();
        out.println("Hello World: " + service.doWork(a, b));
        out.close();

        //Just to call @PreDestroy method in ServiceBean
        request.getSession().invalidate();
    }
}