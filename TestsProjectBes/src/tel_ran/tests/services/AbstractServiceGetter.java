package tel_ran.tests.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tel_ran.tests.services.IService;
import tel_ran.tests.services.fields.Role;
import tel_ran.tests.services.utils.SpringApplicationContext;
import tel_ran.tests.token_cipher.TokenProcessor;
import tel_ran.tests.token_cipher.User;
import tel_ran.tests.utils.errors.AccessException;

@Component
public class AbstractServiceGetter {

	
	private static TokenProcessor tokenProcessor;
	
	static {
		tokenProcessor = (TokenProcessor) SpringApplicationContext.getBean("tokenProc");
	}
		
	public static IService getService(String token, String beanName) throws AccessException {
		User user = encodeToken(token);
		
		
		if(user!=null && user.isAutorized()) {
			IService result = getServiceByBeanName(beanName);
			
			intiateService(result, user);
			
			return result;
		} else {
			throw new AccessException();
		}
	}

	private static void intiateService(IService result, User user) {
		result.setUser(user);
		
	}

	private static IService getServiceByBeanName(String beanName) {		
		return (IService) SpringApplicationContext.getBean(beanName);
	}

	private static User encodeToken(String token) {		
		return tokenProcessor.decodeRoleToken(token);
	}
	
}