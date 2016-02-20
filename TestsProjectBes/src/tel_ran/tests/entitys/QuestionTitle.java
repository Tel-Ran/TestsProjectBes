package tel_ran.tests.entitys;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class QuestionTitle implements Serializable {
	
	private static final long serialVersionUID = 2L;
		
	@Id @GeneratedValue(strategy = GenerationType.AUTO)	
	@Column(name = "id")
	private long id;

	@Column(unique = true, nullable = false, length = 500)
	private String questionTitle; 
	
	
	
	
	public QuestionTitle(){}
		
	public String getQuestionText() {
		return questionTitle;
	}
	public void setQuestionText(String questionText) {
		this.questionTitle = questionText;
	}

	public long getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return id + "----" + questionTitle;// IMaintenanceService.DELIMITER = "----"
	}
}