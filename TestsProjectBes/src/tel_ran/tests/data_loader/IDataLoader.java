package tel_ran.tests.data_loader;

import json_models.AutorizationModel;


public interface IDataLoader {
	
	long checkUserLogIn(String email, String password);
	long checkCompanyLogIn(String name, String password);
	boolean isAdmin(long id);
	boolean checkUserEmail(String email);
	boolean checkRootUser();
	boolean userRegistration(String email, String password);
	boolean userRegistration(String email, String password, boolean isAdmin);
	boolean userRegistration(String email, String password, String firstName, String lastName, String nickName);
	void fillInfoAboutUser(AutorizationModel model, long id);
	void fillInfoAboutCompany(AutorizationModel model, long id);
	boolean checkCompanyName(String login);
	boolean companyRegistration(String login, String password,
			String employesNumber, String webSite, String spec);
	
}