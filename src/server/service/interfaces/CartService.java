package service.interfaces;

import model.dto.TransactionDTO;
import service.exception.ServiceException;

import java.util.Map;

public interface CartService {
    Map<String, Object> getCheckoutContext(String customerName, Map<Integer, Integer> items) throws ServiceException;
    TransactionDTO processSingleOrder(TransactionDTO dto) throws ServiceException;
    Map<String, Object> processCartOrder(String customerName, String paymentMethod, 
                                         String city, Map<Integer, Integer> items) throws ServiceException;
}