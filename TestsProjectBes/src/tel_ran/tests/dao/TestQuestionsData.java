package tel_ran.tests.dao;

import java.util.ArrayList;

import java.util.HashSet;
import java.util.List;

import java.util.Set;

import javax.persistence.Query;

import json_models.IJsonModels;
import json_models.QuestionModel;
import json_models.TemplateModel;

import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import tel_ran.tests.entitys.Category;

import tel_ran.tests.entitys.Company;
import tel_ran.tests.entitys.Person;
import tel_ran.tests.entitys.EntityQuestionAttributes;
import tel_ran.tests.entitys.Test;
import tel_ran.tests.entitys.InTestQuestion;
import tel_ran.tests.entitys.TestTemplate;
import tel_ran.tests.entitys.Texts;
import tel_ran.tests.entitys.QuestionTitle;
import tel_ran.tests.entitys.TemplateCategory;
import tel_ran.tests.entitys.Question;
import tel_ran.tests.entitys.QuestionCustom;
import tel_ran.tests.processor.TestProcessor;
import tel_ran.tests.services.common.ICommonData;
import tel_ran.tests.services.common.IPublicStrings;
import tel_ran.tests.services.fields.Role;

public class TestQuestionsData extends TestsPersistence implements
		IDataTestsQuestions {

	// ------------------------ STATUS OF DB ---------------------------------------------------------------------- //
		
	@Override
	public boolean isNoCategory() {
		Category c = em.find(Category.class, 1);
		if(c==null)
			return true;
		return false;
	}

	@Override
	public boolean isNoQuestions() {
		Question q = em.find(Question.class, 1L);
		if(q==null)
			return true;
		return false;
	}

	// ----------------------------------------------------------------------------------------------------------- //
	
	@Override
	public int getNumberQuestions(int id, Role role) {
		
		StringBuilder query = new StringBuilder("SELECT COUNT(q) from QuestionCustom q");
		
		switch(role) {
			case COMPANY :
				query.append(" WHERE q.").append(getLimitsForCompanyQuery(id)); break;
			case ADMINISTRATOR :
				query.append(" WHERE q.").append(getLimitsForCompanyQuery(ADMIN_C_ID)); break;
			default:
				break;				
		}		
		long result = (Long)em.createQuery(query.toString()).getSingleResult();	
		return (int) result;	
	}
		

	@Override
	public int getNumberTests(int id, Role role) {
		StringBuilder query = new StringBuilder("SELECT COUNT(et) from Test et");
		
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
	public QuestionTitle createQuestionTitle(String questionText) {
		QuestionTitle objectQuestion;		
		
		if(questionText == null) {
			questionText = ICommonData.NO_QUESTION;
		}
		
		//// query if question exist as text in Data Base
		Query tempRes = em.createQuery("SELECT q FROM QuestionTitle q WHERE q.questionTitle=?1").setParameter(1, questionText);
		
		List<QuestionTitle> listTitles = tempRes.getResultList();
		
		if(listTitles==null || listTitles.isEmpty()) {
			objectQuestion = new QuestionTitle();	
			objectQuestion.setQuestionText(questionText);			
			em.persist(objectQuestion);	
		} else {
			objectQuestion = listTitles.get(0);
		}
							
		return objectQuestion;		
		
	}
	
	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRES_NEW) 
	public boolean saveNewQuestion(Question qstn, Category category, Company adminCompany,
			List<Texts> texts) {
		
		//1 - title save
		QuestionTitle title = this.createQuestionTitle(qstn.getTitle().getQuestionText());
		qstn.setTitle(title);
			
		
		//2 - question save
		qstn.setCompany(adminCompany);
		qstn.setCategory(category);
		saveQuestion(qstn);
		
		//3 - texts check and save
		saveTexts(texts, qstn);
		
		return true;
	}
	
	

	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRES_NEW) 
	public boolean saveNewCustomQuestion(QuestionCustom question, List<Texts> text) {
			
		QuestionTitle title = this.createQuestionTitle(question.getTitle().getQuestionText());
		question.setTitle(title);
						
		saveQuestion(question);
		
		saveTexts(text, question);	
				
		return true;
	}
	
	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRES_NEW) 
	public Category createCategory(Category category, Company company) {
		
		Category parent = category.getParentCategory();
		if(parent!=null) {
			createCategory(parent, company);
			category.setParentCategory(parent);
		}
		category.setCompany(company);
		category.setControlName(company.getId());
		System.out.println("cat control name- "+category.getControlName());
		
		return saveCategory(category);	
		
	}
	
	
	@Transactional(readOnly=false) 
	private Category saveCategory(Category category) {
		
		Category checkCategory = findCategoryByControlName(category.getControlName());
		if(checkCategory!=null) {
			System.out.println("CHECK CATEGORY ID = " + checkCategory.getId());
			if(checkCategory.equals(category)) {
				System.out.println("Category equals");				
			} else {
				System.out.println("Category doesn't equal");
				checkCategory.copyCategory(category);				
				em.merge(checkCategory);				
			}	
			return checkCategory;
		} else {
			em.persist(category);
			System.out.println("cat id- "+category.getId());	
		}
		return category;
		
	}


	private Category findCategoryByControlName(String controlName) {
		String query = "SELECT c FROM Category c WHERE c.controlName=?1";
		List<Category> result = em.createQuery(query).setParameter(1, controlName).getResultList();
		if(result==null || result.isEmpty()) {
			System.out.println("No! findCategoryByControlName- ");
			return null;
		}
		return result.get(0);
	}




	@Transactional(readOnly=false, propagation=Propagation.REQUIRES_NEW) 
	private void saveQuestion(Question question) {
		em.persist(question);
	}
	
	@Transactional(readOnly=false, propagation=Propagation.REQUIRES_NEW)
	private void saveTexts(List<Texts> list, Question question) {
		if(list!=null) {
			for(Texts text : list) {
				text.setQuestion(question);
				em.merge(text);
				
			}
			
		}
	}
	
	@Transactional(readOnly=false,propagation=Propagation.REQUIRED) 
	private Texts writeNewAnswer(String answer, EntityQuestionAttributes qAttrId){		
		Texts temp = new Texts();
		temp.setText(answer);		
		temp.setEntityQuestionAttributes(qAttrId);	
		em.persist(temp);	
		em.clear();
		return temp;
	}
	
	@Override
	@Transactional
	public List<String> getUserCategories(int id, Role role) {		

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
			String category1, int id, Role role) {
		
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
	public List<String> getUserMetaCategories(int id, Role role) {	
		
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
		
		Person person;
		String query = "Select p FROM Person p WHERE p.identify='" + personPassport + "'";
		
		System.out.println("createPerson + query- " + query);
		
		List<Person> result = em.createQuery(query).getResultList();
		if(result!=null && !result.isEmpty()) {
			person = result.get(0);
			
		} else {
			person = new Person();
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
	public List<Long> getQuestionIdByParams(int id, Role role, String metaCategory,
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
			long stopTime, List<Long> questionIdList, int id, Role role) {
		long result = -1;
				
		Test test = new Test();		
		test.setPassword(pass); 
		test.setStartTestDate(startTime);// setting parameter for wotchig method in FES
		test.setEndTestDate(stopTime);// setting parameter for wotchig method in FES		
			
		Company ec = null;
		if(role.equals(Role.COMPANY)) {
			ec = em.find(Company.class, id);
		}
		test.setCompany(ec);
		
		Person ePerson = em.find(Person.class, personId);
		test.setPerson(ePerson);	
		test.setAmountOfQuestions(questionIdList.size());
		test.setPassed(false);
		test.setChecked(false);
		em.persist(test);
		
		result = test.getId();
		
		
		for(Long qId : questionIdList) {
			Question eqa = em.find(Question.class, qId);
			InTestQuestion etq = new InTestQuestion();
			etq.setQuestion(eqa);
			etq.setTest(test);
			etq.setStatus(ICommonData.STATUS_NO_ANSWER);
			em.persist(etq);
			test.addInTestQuestions(etq);
		}
		
		em.merge(test);		

		return result;
		
	}

	// ------------------------- LISTS OF CATEGORIES ------------------------------------- //
	
	// LIST OF CUSTOM CATEGORIES
	@Override
	public List<Category> getCategoriesList(Role role, int companyId) {
				
		//1 - check ID
		companyId = getCompanyId(role, companyId);		
		if(companyId<0) return null;
				
		//2 - get List of categories1
		List<Category> categories = getCustomCategoriesByCompanyId(companyId);
				
		return categories;
	}
		

	private List<Category> getCustomCategoriesByCompanyId(int companyId) {
		String query = "SELECT c FROM CategoryCustom c WHERE c." + getLimitsForCompanyQuery(companyId);
		List<Category> list = em.createQuery(query).getResultList();			
		return list;
	}


	@Override
	public List<String> getCategories(int companyId, int categoryLevel,
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
			textQuery.append(" AND");
		}
		
		
		textQuery.append(" c.");		
		if(companyId<0) {
			textQuery.append(getLimitsForNotCompanyQuery());			
		} else {
			textQuery.append(getLimitsForCompanyQuery(companyId));
		}
		
		textQuery.append(" ORDER BY c.").append(categoryType);			
		result = em.createQuery(textQuery.toString()).getResultList();		
		
		return result;
	}
	
	
	// -------------------------------------- TEST TEMPLATES --------------------------------------------- //
	
	@Override
	@Transactional(readOnly=false)
	public void createTemplate(TemplateModel template, Role role, long companyId) {		
		Company company = em.find(Company.class, getCompanyId(role, companyId));
		TestTemplate testTemplate = template.getTemplate();
		testTemplate.setCompany(company);
		String templateName = testTemplate.getTemplateName();
		if(templateName==null) testTemplate.setTemplateName("New Template");
		em.persist(testTemplate);
		
		if(template.hasQuestionsList()) {
			Set<Long> questionsId = template.getQuestionsId();
			Set<Question> questions = new HashSet<Question>();
			for(long questionId : questionsId) {
				Question q = findQuestionById(questionId);
				questions.add(q);
			}
			testTemplate.setQuestions(questions);
			em.merge(testTemplate);
		}
		
		if(template.hasCategories()) {
			Set<TemplateCategory> categories = template.getCategories();
			
			for(TemplateCategory cat : categories) {
				cat.setTemplate(testTemplate);
				em.persist(cat);			
			}
		}
		
	}

	@Override
	public List<Question> getQuestionsByParams(TemplateCategory tCategory) {
		
		StringBuilder queryString = new StringBuilder("SELECT c FROM");
		
		String type = tCategory.getTypeOfQuestion();
		if(type==null) {
			queryString.append(" Question");
		} else if(type.equals(IPublicStrings.COMPANY_AMERICAN_TEST)) {
			queryString.append(" QuestionCustomTest");
		} else if(type.equals(IPublicStrings.COMPANY_QUESTION)) {
			queryString.append(" QuestionCustomOpen");
		} else {
			queryString.append(" Question");
		}
		
		queryString.append(" c WHERE c.category=?1 AND c.levelOfDifficulty=?2");
		Query q = em.createQuery(queryString.toString());
		q.setParameter(1, tCategory.getCategory());
		q.setParameter(2, tCategory.getDifficulty());
		
		List<Question> result = q.getResultList();
		
		return result;
	}
	

	@Override
	public List<Question> getAllQuestionsByParams(Category category, int difficulty, Role role, int id,
			boolean isAdmin) {
		
		if(isAdmin) id = ADMIN_C_ID;
		else {
			id = getCompanyId(role, id);
		}
		
		StringBuilder queryString = new StringBuilder("SELECT c FROM Question c WHERE c.");
		queryString.append(getLimitsForCompanyQuery(id));
		
		if(category!=null) {
			queryString.append(" AND c.category='").append(category.getId()).append("'");
		}
				
		if(difficulty>0) {
			queryString.append(" AND c.levelOfDifficulty='").append(difficulty).append("'");
		}
		
		List<Question> result = em.createQuery(queryString.toString()).getResultList();
				
		return result;
		
	}

	@Override
	public List<Question> getAmericanTestsByParams(Category category, int difficulty, Role role, int id,
			boolean isAdmin) {
		
		if(isAdmin) id = ADMIN_C_ID;
		else {
			id = getCompanyId(role, id);
		}
		
		StringBuilder queryString = new StringBuilder("SELECT c FROM QuestionCustomTest c WHERE c.");
		queryString.append(getLimitsForCompanyQuery(id));
		
		if(category!=null) {
			queryString.append(" AND c.category='").append(category.getId()).append("'");
		}
				
		if(difficulty>0) {
			queryString.append(" AND c.levelOfDifficulty='").append(difficulty).append("'");
		}
		
		List<Question> result = em.createQuery(queryString.toString()).getResultList();
				
		return result;
		
	}
	
	@Override
	public List<Question> getOpenQuestionsByParams(Category category, int difficulty, Role role, int id,
			boolean isAdmin) {
		if(isAdmin) id = ADMIN_C_ID;
		else {
			id = getCompanyId(role, id);
		}
		
		StringBuilder queryString = new StringBuilder("SELECT c FROM QuestionCustomOpen c WHERE c.");
		queryString.append(getLimitsForCompanyQuery(id));
		
		if(category!=null) {
			queryString.append(" AND c.category='").append(category.getId()).append("'");
		}
				
		if(difficulty>0) {
			queryString.append(" AND c.levelOfDifficulty='").append(difficulty).append("'");
		}
		
		List<Question> result = em.createQuery(queryString.toString()).getResultList();
				
		return result;
		
	}
		
	@Override
	public Question findQuestionById(Long id) {		
		return (Question) em.find(Question.class, id);
	}

	@Override
	@Transactional(readOnly=false)
	public long createPerson(Person entity) {
		em.persist(entity);		 
		return entity.getPersonId();
		
	}

	@Override
	@Transactional(readOnly=false)	
	public long createTest(Test test, Set<Question> questions, Person person, Role role, int id) {
		
		test.setCompany(em.find(Company.class, getCompanyId(role, id)));
		test.setPerson(person);
		em.persist(test);
		
		for(Question q : questions) {
			InTestQuestion tQuestion = new InTestQuestion();
			tQuestion.setQuestion(q);
			tQuestion.setTest(test);			
			em.persist(tQuestion);
		}
		return test.getId();		
	}
	
	@Override
	@Transactional
	public Test findTestById(long testId) {		
		Test test = em.find(Test.class, testId);
		Hibernate.initialize(test.getInTestQuestions());
		return test;
	}
	
	//------------------------------------ INNER METHODS ------------------------------------------------//
	private String getLimitsForCompanyQuery(int id) {	
		return "company='" + id + "'";
	}
	
	private String getLimitsForNotCompanyQuery() {	
		return getLimitsForCompanyQuery(ADMIN_C_ID);
	}
	
	private int getCompanyId(Role role, long companyId) {
		if(role.equals(Role.ADMINISTRATOR)) return ADMIN_C_ID;
		return (int)companyId;
	}
	
	@Override
	public List<TestTemplate> getTemplates(int id) {
		
		if(id<0) id = 1;
		String queryText = "SELECT c FROM TestTemplate c WHERE c." + getLimitsForCompanyQuery(id);
		
		List<TestTemplate> templates = em.createQuery(queryText).getResultList();
		
		return templates;
	}
	
	@Override
	@Transactional
	public TestTemplate getTemplate(long templateId) {
		TestTemplate template = em.find(TestTemplate.class, templateId);
		Hibernate.initialize(template.getQuestions());
		Hibernate.initialize(template.getCategories());
		
		
		return template;
	}

	@Override
	@Transactional(readOnly=false)
	public void createCategory(Category cat, int id, Role role) {		
		Company company = getCompanyById(id, role);
		cat.setCompany(company);
		cat.setControlName(company.getId());
		try {
			em.persist(cat);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}


	@Override
	public List<Category> getAutoCategoriesList() {
		String query = "SELECT c FROM CategoryGenerated c";
		List<Category> categories = em.createQuery(query).getResultList();
				
		return categories;
	}

	@Override
	public Category getCategory(int categoryId) {
		Category category = em.find(Category.class, categoryId);
		
		return category;
	}

	@Override
	@Transactional
	public InTestQuestion findTestQuestionById(long tQuestionId) {
		if(tQuestionId<=0) return null;
		InTestQuestion result =  em.find(InTestQuestion.class, tQuestionId);
		Hibernate.initialize(result.getTest());
		Hibernate.initialize(result.getTest().getCompany());
		Hibernate.initialize(result.getQuestion());
		return result;
	}

	@Override
	@Transactional(readOnly=false)
	public void saveAnswer(InTestQuestion tQuestion) {
		em.merge(tQuestion);		
	}

	@Override
	@Transactional(readOnly=false)
	public void saveTest(Test test) {
		em.merge(test);		
	}

	@Override
	@Transactional
	public Question initiateQuestionInTest(InTestQuestion tQuestion) {
		Question question = tQuestion.getQuestion();
		
		
		
		return question;
	}

	@Override
	@Transactional
	public TestTemplate getTemplateForResults(long template_id, long id, Role role) {
		TestTemplate template = em.find(TestTemplate.class, template_id);
		int companyId = getCompanyId(role, id);
		int companyIdOfTemplate = template.getCompany().getId();
		if(companyIdOfTemplate==companyId || companyIdOfTemplate==ADMIN_C_ID) {
			Hibernate.initialize(template.getCategories());
			return template;
		}
		
		return null;
	}

	@Override
	@Transactional
	public List<Test> getFinishedTestsForTemplate(TestTemplate template, long id, Role role) {
		
		String queryText = "SELECT t FROM Test t WHERE t.baseTemplate=?1 AND t.isPassed=?2 AND t.company=?3";
		Query query = em.createQuery(queryText)
				.setParameter(1, template)
				.setParameter(2, true)
				.setParameter(3, getCompanyId(role, id));
				
		return query.getResultList();
	}	
}