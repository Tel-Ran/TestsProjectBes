package tel_ran.tests.entitys;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import json_models.JSONKeys;
import tel_ran.tests.services.processes.CategoryResults;

@Entity
public class Test {


	////
	@Id
	@GeneratedValue
	private long id; 
	
	/**
	 * Password is used to find the test. It should be unique.
	 */
	@Column(name = "password", unique = true, nullable = false)	
	private String password;
	
	/**
	 * True if the Person has started answering the test and finished it. Or the time after the start is passed (???)
	 */
	private boolean isPassed;
	
	/**
	 * True if all the questions in the test were checked. It's important for open questions where company should
	 * decide if the answer is correct or not. 
	 */
	private boolean isChecked;
	
	@Column(name = "cam_prntscr")
	private boolean usesCameraANDPrintScreen;
		
	private int amountOfCorrectAnswers;		
	private int amountOfQuestions;
	////
	////
	private int duration;
	private long startTestDate = 0L;
	private long endTestDate = 0L;
	//
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "test")
	private List<InTestQuestion> questions;
	
	@ManyToOne
	private Company company;
	@ManyToOne
	private Person person;
	
	@ManyToOne
	private EntityUser user;
	
	private String linkToTest;
	private boolean sendedByMail;
	/**
	 * If the test was created by some Test Template here we have
	 * a link to this Template 
	 */
	@ManyToOne
	private TestTemplate baseTemplate;
	
	@Column(length=2000)
	private String results;
		
	public Test() {	

	}
		
	public String getResults() {
		return results;
	}

	public void setResults(String results) {
		this.results = results;
	}



	public TestTemplate getBaseTemplate() {
		return baseTemplate;
	}
	
	public void setBaseTemplate(TestTemplate baseTemplate) {
		this.baseTemplate = baseTemplate;
	}



	public void setAmountOfCorrectAnswers(int amountOfCorrectAnswers) {
		this.amountOfCorrectAnswers = amountOfCorrectAnswers;
	}
	public void setAmountOfQuestions(int amountOfQuestions) {
		this.amountOfQuestions = amountOfQuestions;
	}

	public long getId() {
		return id;
	}

	public String getPassword() {
		if(this.password==null || this.password.isEmpty())
			resetRandomPassword();
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getAmountOfCorrectAnswers() {
		return amountOfCorrectAnswers;
	}

	public int getAmountOfQuestions() {
		return amountOfQuestions;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public long getStartTestDate() {
		return startTestDate;
	}

	public void setStartTestDate(long startTestDate) {
		this.startTestDate = startTestDate;
	}

	public long getEndTestDate() {
		return endTestDate;
	}

	public void setEndTestDate(long endTestDate) {
		this.duration = (int) (endTestDate - this.startTestDate); //// set duration in m_sec
		this.endTestDate = endTestDate;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person entityPerson) {
		this.person = entityPerson;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company entityCompany) {
		this.company = entityCompany;
	} 

	public void setPassed(boolean isPassed) {
		this.isPassed = isPassed;
	}
	public boolean isPassed() {
		return isPassed;
	}
	public boolean isUsesCameraANDPrintScreen() {
		return usesCameraANDPrintScreen;
	}
	public void setUsesCameraANDPrintScreen(boolean usesCameraANDPrintScreen) {
		this.usesCameraANDPrintScreen = usesCameraANDPrintScreen;
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

	public List<InTestQuestion> getInTestQuestions() {
		return questions;
	}

	public void setInTestQuestions(List<InTestQuestion> entityTestQuestions) {
		this.questions = entityTestQuestions;
	}
	
	public void addInTestQuestions(InTestQuestion entityTestQuestions) {
		if(this.questions==null) 
			this.questions = new ArrayList<InTestQuestion>();
		this.questions.add(entityTestQuestions);		
	}

	public void generateLink(String path) {
		this.linkToTest = path.concat("?").concat(getPassword());				
	}
	
	public void resetRandomPassword() {
		this.password = UUID.randomUUID().toString();		
	}

	public String getLinkToTest() {
		return linkToTest;
	}

	public void setLinkToTest(String linkToTest) {
		this.linkToTest = linkToTest;
	}

	public boolean isSendedByMail() {
		return sendedByMail;
	}

	public void setSendedByMail(boolean sendedByMail) {
		this.sendedByMail = sendedByMail;
	}	
	
	public void createJsonResult(Collection<CategoryResults> catResults, int numAnswers) throws JSONException {
		JSONObject jsn = new JSONObject();
		
		
		JSONObject jsnTest = new JSONObject();
		jsnTest.put(JSONKeys.RESULTS_TEST_ID, id);
		jsnTest.put(JSONKeys.RESULTS_TEST_NUM_QUESTIONS, amountOfQuestions);
		jsnTest.put(JSONKeys.RESULTS_TEST_NUM_ANSWERS, numAnswers);
		float result = ((float)this.amountOfCorrectAnswers) / this.amountOfQuestions;
		
		System.out.println(result);
		String res2 = Float.toString(result);
		jsnTest.put(JSONKeys.RESULTS_TEST_TOTAL_RESULT, res2);
		jsnTest.put(JSONKeys.RESULTS_TEST_MANUAL_CHECK_PENDING, !this.isChecked);		
		jsn.put(JSONKeys.RESULTS_TEST_DATA, jsnTest);
		
		
		JSONArray array = new JSONArray();
		
		for(CategoryResults res : catResults) {
			array.put(res.getJson());			
		}
		jsn.put(JSONKeys.RESULTS_BY_CATEGORY, array);
		
		JSONObject jsnPerson = this.person.getJSON();
		jsn.put(JSONKeys.RESULTS_PERSON_DATA, jsnPerson);
		
		this.results = jsn.toString();
		
	}
	
	
	
}
