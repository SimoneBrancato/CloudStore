package com.cloudstore.server.service.interfaces;

import com.cloudstore.server.service.exception.ServiceException;

import java.util.List;
import java.util.Map;

public interface DashboardService {
    
    Map<String, Object> getDashboardStats() throws ServiceException;

    Map<String, Object> getUserProfile(String nickname) throws ServiceException;

    Map<String, Object> getSellerDashboardStats() throws ServiceException;

    List<?> getSellerProducts() throws ServiceException;

    List<Map<String, Object>> getSellerSalesOrders(int limit) throws ServiceException;

    List<Map<String, Object>> getSellerTopCustomers(int limit) throws ServiceException;
}