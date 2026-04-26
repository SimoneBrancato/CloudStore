package com.cloudstore.server.service.interfaces;

import com.cloudstore.server.model.entities.*;
import com.cloudstore.server.model.domain.*;
import com.cloudstore.server.service.exception.ServiceException;

import java.util.List;

public interface DashboardService {
    
    DashboardStats getDashboardStats() throws ServiceException;

    UserProfile getUserProfile(String nickname) throws ServiceException;

    SellerDashboardStats getSellerDashboardStats() throws ServiceException;

    List<Product> getSellerProducts() throws ServiceException;

    List<SalesOrderSummary> getSellerSalesOrders(int limit) throws ServiceException;

    List<TopCustomerSummary> getSellerTopCustomers(int limit) throws ServiceException;
}