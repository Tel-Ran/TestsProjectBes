package tel_ran.tests.entitys;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import tel_ran.tests.services.interfaces.IMaintenanceService;

@Entity
public class EntityQuestionAttributes implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	////
	public EntityQuestionAttributes() {	}
	////
	@Id
	@GeneratedValue
	@Column(name = "id")
	private long id;
	////
	@ManyToOne
	private EntityQuestion questionId;
	////
	@OneToMany(mappedBy = "questionAttributeId")
	List<EntityAnswersText> questionAnswersList;
	//// pattern: D785JHGYT785J58R86JJ6776867TRJJ677TJ575JJ584K493K45J55.jpg or any resolution .HashCode.png = this question in hash code for equals action question parameters
	@Column(name = "imageLink", unique = false, nullable = true, length = 500)
	private String imageLink;
	////
	@Column(name = "category")
	private String category;
	////
	@Column(name = "levelOfDifficulti")
	private int levelOfDifficulti;
	////
	@Column(name = "correctAnswer")
	private char correctAnswer;
	////
	public EntityQuestion getQuestionId() {
		return questionId;
	}
	public void setQuestionId(EntityQuestion questionId) {
		this.questionId = questionId;
	}
	////
	public String getImageLink() {
		return imageLink;
	}
	public void setImageLink(String imageLink) {
		this.imageLink = imageLink;
	}
	////
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	////
	public int getLevelOfDifficulti() {
		return levelOfDifficulti;
	}
	public void setLevelOfDifficulti(int levelOfDifficulti) {
		this.levelOfDifficulti = levelOfDifficulti;
	}
	////
	public char getCorrectAnswer() {
		return correctAnswer;
	}
	public void setCorrectAnswer(char correctAnswer) {
		this.correctAnswer = correctAnswer;
	}
	////
	public List<EntityAnswersText> getQuestionAnswersList() {
		return questionAnswersList;
	}
	public void setQuestionAnswersList(List<EntityAnswersText> questionAnswers) {
		this.questionAnswersList = questionAnswers;
	}
	////
	public long getId() {
		return id;
	}
	////
	@Override
	public String toString() {
		return questionId.getId()
				+ IMaintenanceService.DELIMITER + imageLink 
				+ IMaintenanceService.DELIMITER + category
				+ IMaintenanceService.DELIMITER + levelOfDifficulti
				+ IMaintenanceService.DELIMITER + correctAnswer;
	}
}