package service.interfaces;

import service.exception.ServiceException;

import java.util.Map;

public interface DashboardService {
    
    Map<String, Object> getDashboardStats() throws ServiceException;

    Map<String, Object> getUserProfile(String nickname) throws ServiceException;
}