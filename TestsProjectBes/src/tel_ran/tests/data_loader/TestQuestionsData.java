package tel_ran.tests.data_loader;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import json_models.IJsonModels;
import json_models.QuestionModel;
import json_models.SimpleArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import tel_ran.tests.entitys.EntityCompany;
import tel_ran.tests.entitys.EntityPerson;
import tel_ran.tests.entitys.EntityQuestionAttributes;
import tel_ran.tests.entitys.EntityTest;
import tel_ran.tests.entitys.EntityTestQuestions;
import tel_ran.tests.entitys.EntityTexts;
import tel_ran.tests.entitys.EntityTitleQuestion;
import tel_ran.tests.processor.TestProcessor;
import tel_ran.tests.services.common.ICommonData;
import tel_ran.tests.services.common.IPublicStrings;
import tel_ran.tests.services.fields.Role;

public class TestQuestionsData extends TestsPersistence implements
		IDataTestsQuestions {

	@Override
	public int getNumberQuestions(long id, Role role) {
		
		StringBuilder query = new StringBuilder("SELECT COUNT(eqa) from EntityQuestionAttributes eqa");
		
		switch(role) {
			case COMPANY :
				query.append(" WHERE eqa.").append(getLimitsForCompanyQuery(id)); break;
			default:
				break;				
		}		
		long result = (Long)em.createQuery(query.toString()).getSingleResult();	
		return (int) result;	
	}
		

	@Override
	public int getNumberTests(long id, Role role) {
		StringBuilder query = new StringBuilder("SELECT COUNT(et) from EntityTest et");
		
		switch(role) {
		case COMPANY :
			query.append(" WHERE et.").append(getLimitsForCompanyQuery(id)); break;
		case USER :		
		case GUEST :
		case PERSON: return 0; 
		default:
			break;				
		}
		long result = (Long)em.createQuery(query.toString()).getSingleResult();	
		return (int) result;	
	}
	
	//-------------------------------------QUESTIONS ---------------------------------------------------//
	
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED) 
	public EntityTitleQuestion createQuestion(String questionText) {
		EntityTitleQuestion objectQuestion;		
		
		if(questionText == null) {
			questionText = ICommonData.NO_QUESTION;
		}
		//// query if question exist as text in Data Base
		Query tempRes = em.createQuery("SELECT q FROM EntityTitleQuestion q WHERE q.questionText='"+questionText+"'");
		try{			
			objectQuestion = (EntityTitleQuestion) tempRes.getSingleResult();			
			
		}catch(javax.persistence.NoResultException e){				
			objectQuestion = new EntityTitleQuestion();	
			objectQuestion.setQuestionText(questionText);			
			em.persist(objectQuestion);			
		}								
		return objectQuestion;		
		
	}
	
	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRES_NEW) 
	public boolean saveNewQuestion(String fileLocationLink, String metaCategory,
			String category1, String category2, int levelOfDifficulty,
			List<String> answers, String correctAnswerChar, int answerOptionsNumber, String description,
			String questionText, long id, Role role) {
		boolean result = false;
		
		EntityTitleQuestion etq = createQuestion(questionText);
	
		EntityQuestionAttributes questionAttributesList = new EntityQuestionAttributes();// new question attributes creation		
		
		questionAttributesList.setDescription(description);		
		if(role.equals(Role.COMPANY)) {	
			EntityCompany ec = em.find(EntityCompany.class, id);
			questionAttributesList.setCompanyId(ec);
		}				
		questionAttributesList.setEntityTitleQuestion(etq);	
		questionAttributesList.setFileLocationLink(fileLocationLink);	
		questionAttributesList.setMetaCategory(metaCategory);
		questionAttributesList.setCategory1(category1);		
		questionAttributesList.setCategory2(category2);
		questionAttributesList.setLevelOfDifficulty(levelOfDifficulty);	
		questionAttributesList.setCorrectAnswer(correctAnswerChar);		
		questionAttributesList.setNumberOfResponsesInThePicture(answerOptionsNumber);
		em.persist(questionAttributesList);	
		
		if(answers != null)	{			
			List<EntityTexts> answersList = new ArrayList<EntityTexts>();
			for (String answerText : answers) {	
				
				if(answerText!=null && answerText.length()>0) {
					EntityTexts ans = writeNewAnswer(answerText, questionAttributesList); 
					answersList.add(ans);
				}
			}			
			questionAttributesList.setQuestionAnswersList(answersList);// mapping to answers
			em.merge(questionAttributesList);	
		}				
		em.merge(etq);		
		
		result = true;
		return result;
	}
	
	@Transactional(readOnly=false,propagation=Propagation.REQUIRED) 
	private EntityTexts writeNewAnswer(String answer, EntityQuestionAttributes qAttrId){		
		EntityTexts temp = new EntityTexts();
		temp.setAnswerText(answer);		
		temp.setEntityQuestionAttributes(qAttrId);	
		em.persist(temp);	
		em.clear();
		return temp;
	}
	
	@Override
	@Transactional
	public List<String> getUserCategories(long id, Role role) {		

		StringBuilder query = new StringBuilder("Select DISTINCT q.category1 FROM EntityQuestionAttributes q WHERE (q.metaCategory='");
		query.append(IPublicStrings.COMPANY_AMERICAN_TEST).append("' OR q.metaCategory='").append(IPublicStrings.COMPANY_QUESTION).
			append("') AND q.category1 is not null");
		
		switch(role) {
			case COMPANY :
				query.append(" AND q.").append(getLimitsForCompanyQuery(id)); break;
			case ADMINISTRATOR :
				query.append(" AND q.").append(getLimitsForNotCompanyQuery()); break;
			case USER :		
			case GUEST :
			case PERSON: 
			default:		
		}	
		query.append(" ORDER BY q.category1");	
		List<String> result = em.createQuery(query.toString()).getResultList();		
		
		return result;
	}

	
	@Override
	@Transactional
	public List<IJsonModels> getQuesionsList(Boolean typeOfQuestion, String metaCategory,
			String category1, long id, Role role) {
		
		//1 - start query 
		StringBuilder query = new StringBuilder("SELECT c FROM EntityQuestionAttributes c WHERE ");
		boolean needAND = false;
		
		//2 - adding id or other limits
		switch(role) {
		case COMPANY :
			query.append(" c.").append(getLimitsForCompanyQuery(id)); needAND = true; break;
		case ADMINISTRATOR :
			query.append(" c.").append(getLimitsForNotCompanyQuery()); needAND = true; break;
		case USER :		
		case GUEST :
		case PERSON: 
		default:		
		}	
		
		//3 - adding metaCategories		
		if(needAND) {
			query.append(" AND");			
		}	
		
		if(metaCategory==null) {
			
			if(typeOfQuestion) {								
				query.append(" (c.metaCategory='").append(IPublicStrings.COMPANY_AMERICAN_TEST)
						.append("' OR c.metaCategory='").append(IPublicStrings.COMPANY_QUESTION)
						.append("')");
				} else {
					List<String> cat = TestProcessor.getMetaCategory();
					int count = cat.size();
					query.append(" (");
					for(String c : cat) {
						query.append("c.metaCategory='").append(c).append("' OR ");					
					}
					int len = query.length();						
					query.delete(len-4, len);
					query.append(")");				
				}
		} else {					
			query.append(" c.metaCategory=").append(metaCategory);			
		}
		
			
		
		//4 - adding categories		
		if(category1!=null) {		
			query.append(" AND");
			query.append(" c.category1=").append(category1);			
		}
		
		//5 - adding sort		
		query.append(" ORDER BY c.id DESC");		
		System.out.println(query.toString()); // ---------------------------------------- SYSO ---- !!!!!!!!!!!!!!!!!!!!!
						
		//6 - get result 
		List<EntityQuestionAttributes> listOfEqa = em.createQuery(query.toString()).getResultList();
		List<IJsonModels> resultList = null;
		
		if(!listOfEqa.isEmpty()) {			
			resultList = new ArrayList<IJsonModels>();
			for(EntityQuestionAttributes eqa : listOfEqa) {
				boolean isImage;
				if(eqa.getFileLocationLink()!=null) {
					isImage = true;				
				} else {
					isImage = false;			
				}
				QuestionModel question = new QuestionModel(eqa.getId(), eqa.getMetaCategory(), 
						eqa.getCategory1(), eqa.getCategory2(), eqa.getLevelOfDifficulty(), isImage);				
				question.addShortDescription(eqa.getDescription());
				resultList.add(question);					
			}			
		}
		return resultList;
	}
			
	

	@Override
	public List<String> getUserMetaCategories(long id, Role role) {	
		
		StringBuilder query = new StringBuilder("Select DISTINCT cat.metaCategory FROM EntityQuestionAttributes cat WHERE cat.metaCategory is not null");
		switch(role) {
			case COMPANY :
				query.append(" AND cat.").append(getLimitsForCompanyQuery(id)); break;
			case ADMINISTRATOR :
			case USER :
				query.append(" AND cat.").append(getLimitsForNotCompanyQuery()); break;
			default:				
		}
		query.append(" ORDER BY cat.metaCategory");		
		
		return em.createQuery(query.toString()).getResultList();
	}
	
	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRES_NEW) 
	public long createPerson(String personPassport, String personName,
			String personSurname, String personEmail) {		
		
		EntityPerson person;
		String query = "Select p FROM EntityPerson p WHERE p.identify='" + personPassport + "'";
		
		System.out.println("I'm here + " + query);
		
		List<EntityPerson> result = em.createQuery(query).getResultList();
		if(result!=null && !result.isEmpty()) {
			person = result.get(0);
			
		} else {
			person = new EntityPerson();
			person.setIdentify(personPassport);
		}
		if(personEmail!=null && personEmail.length()>1)
			person.setPersonEmail(personEmail);
		if(personName!=null && personEmail.length()>1)
			person.setPersonName(personName);
		if(personSurname!=null && personEmail.length()>1)
			person.setPersonSurname(personSurname);
			
		em.persist(person);	
		
		return person.getPersonId();		
	}
	

	@Override
	public List<Long> getQuestionIdByParams(long id, Role role, String metaCategory,
			String category1, int level) {
		
			StringBuilder condition = new StringBuilder("SELECT c.id FROM EntityQuestionAttributes c WHERE ");
			condition.append("c.metaCategory=?1 AND c.levelOfDifficulty=?2");
			
			if(role.equals(Role.COMPANY)) {
				condition.append(" AND c.").append(getLimitsForCompanyQuery(id));
			} else {
				condition.append(" AND c.").append(getLimitsForNotCompanyQuery());
			}
			
			boolean c1 = false;
			if(category1!=null && !category1.equals(ICommonData.NO_CATEGORY1)) {
				condition.append(" AND c.category1=?3");
				c1 = true;
			}
			
			Query query = em.createQuery(condition.toString());
			query.setParameter(1, metaCategory);
			query.setParameter(2, level);
			if(c1) {			
				query.setParameter(3, category1);
			}
			
		return query.getResultList();	
	}

	
	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRES_NEW) 
	public long createTest(String pass, long personId, long startTime,
			long stopTime, List<Long> questionIdList, long id, Role role) {
		long result = -1;
				
		EntityTest test = new EntityTest();		
		test.setPassword(pass); 
		test.setStartTestDate(startTime);// setting parameter for wotchig method in FES
		test.setEndTestDate(stopTime);// setting parameter for wotchig method in FES		
			
		EntityCompany ec = null;
		if(role.equals(Role.COMPANY)) {
			ec = em.find(EntityCompany.class, id);
		}
		test.setEntityCompany(ec);
		
		EntityPerson ePerson = em.find(EntityPerson.class, personId);
		test.setEntityPerson(ePerson);	
		test.setAmountOfQuestions(questionIdList.size());
		test.setPassed(false);
		test.setChecked(false);
		em.persist(test);
		
		result = test.getId();
		
		
		for(Long qId : questionIdList) {
			EntityQuestionAttributes eqa = em.find(EntityQuestionAttributes.class, qId);
			EntityTestQuestions etq = new EntityTestQuestions();
			etq.setEntityQuestionAttributes(eqa);
			etq.setEntityTest(test);
			etq.setStatus(ICommonData.STATUS_NO_ANSWER);
			em.persist(etq);
			test.addEntityTestQuestions(etq);
		}
		
		em.merge(test);		

		return result;
		
	}


	@Override
	public List<String> getCategories(long companyId, int categoryLevel,
			String parent, int levelOfParent) {
		List<String> result = null;
		String categoryType;
		String parentType = null;
		StringBuilder textQuery = new StringBuilder("SELECT DISTINCT c.");
		switch(categoryLevel) {
			case 0 : categoryType = "metaCategory"; break;
			case 1 : categoryType = "category1"; break;
			case 2 : categoryType = "category2"; break;
			default: return result;
		}		
		textQuery.append(categoryType);
		
		textQuery.append(" FROM EntityQuestionAttributes c WHERE");
		
		if(levelOfParent>=0) {
			switch(levelOfParent) {
				case 0 : parentType = "metaCategory"; break;
				case 1 : parentType = "category1"; break;			
			}
			textQuery.append(" c.").append(parentType);
			if(parent==null) {
				textQuery.append(" is null");
			} else {
				textQuery.append("='").append(parent).append("'");
			}
		}
		
		
		textQuery.append(" AND c.");		
		if(companyId<0) {
			textQuery.append(getLimitsForNotCompanyQuery());			
		} else {
			textQuery.append(getLimitsForCompanyQuery(companyId));
		}
		
		textQuery.append(" ORDER BY c.").append(categoryType);
			
		result = em.createQuery(textQuery.toString()).getResultList();		
		
		return result;
	}

	
	
	
	//------------------------------------ INNER METHODS ------------------------------------------------//
	private String getLimitsForCompanyQuery(long id) {	
//		EntityCompany ec = em.find(EntityCompany.class, id);
		return "entityCompany='" + id + "'";
	}
	
	private String getLimitsForNotCompanyQuery() {	
		return "entityCompany is null";
	}











	
	




	
}
