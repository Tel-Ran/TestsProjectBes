package tel_ran.tests.entitys;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class EntityTest {


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
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "entityTest")
	private List<EntityTestQuestions> entityTestQuestions;
	
	@ManyToOne
	private EntityCompany entityCompany;
	@ManyToOne
	private EntityPerson entityPerson;
		
	public EntityTest() {}

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

	public EntityPerson getEntityPerson() {
		return entityPerson;
	}

	public void setEntityPerson(EntityPerson entityPerson) {
		this.entityPerson = entityPerson;
	}

	public EntityCompany getEntityCompany() {
		return entityCompany;
	}

	public void setEntityCompany(EntityCompany entityCompany) {
		this.entityCompany = entityCompany;
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

	public List<EntityTestQuestions> getEntityTestQuestions() {
		return entityTestQuestions;
	}

	public void setEntityTestQuestions(List<EntityTestQuestions> entityTestQuestions) {
		this.entityTestQuestions = entityTestQuestions;
	}
	
	public void addEntityTestQuestions(EntityTestQuestions entityTestQuestions) {
		if(this.entityTestQuestions==null) 
			this.entityTestQuestions = new ArrayList<EntityTestQuestions>();
		this.entityTestQuestions.add(entityTestQuestions);		
	}
	
	
	
}
