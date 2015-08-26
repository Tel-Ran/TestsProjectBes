package tel_ran.tests.services;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

import javax.persistence.TypedQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import tel_ran.tests.entitys.EntityAnswersText;
import tel_ran.tests.entitys.EntityQuestionAttributes;
import tel_ran.tests.entitys.EntityTest;
import tel_ran.tests.entitys.EntityTestQuestions;
import tel_ran.tests.processor.TestProcessor;
import tel_ran.tests.services.common.ICommonData;
import tel_ran.tests.services.common.IPublicStrings;
import tel_ran.tests.services.interfaces.ICommonService;
import tel_ran.tests.services.utils.FileManagerService;

public abstract class CommonServices extends TestsPersistence implements ICommonService {
	

	protected List<String> getQuery(String query) {		
		TypedQuery<String> q = em.createQuery(query, String.class);
		List<String> allCategories = q.getResultList();		
		return allCategories;
	}
	
	abstract protected String getLimitsForQuery();
			
	public List<String> getAllCategories1FromDataBase() {
		String qry = "Select DISTINCT q.category1 FROM EntityQuestionAttributes q WHERE q.category1 is not null";
		String q = getLimitsForQuery();
		if(q!=null)
			qry = qry.concat(" AND q.").concat(q);
		qry = qry.concat(" ORDER BY q.category1");

		return getQuery(qry);
	}

	
	public List<String> getAllCategories2FromDataBase() {
		String query = "Select DISTINCT q.category2 FROM EntityQuestionAttributes q WHERE q.category2 is not null";
		String qry = getLimitsForQuery();
		if(qry!=null)
			query = query.concat(" AND q.").concat(qry);
		query = query.concat(" ORDER BY q.category2");
		return getQuery(query);
	}
	
	public List<String> getAllMetaCategoriesFromDataBase() {
		
		StringBuilder query = new StringBuilder("Select DISTINCT cat.metaCategory FROM EntityQuestionAttributes cat WHERE cat.metaCategory is not null");	
		String qry = getLimitsForQuery();
		if(qry!=null)
			query.append(" AND cat.").append(qry);
		query.append(" ORDER BY cat.metaCategory");
		return getQuery(query.toString());
	}	
	
	protected String getQuestionWithDelimeters(EntityQuestionAttributes eqa) {
		StringBuilder result = new StringBuilder("");
		result.append(eqa.getId()).append(DELIMITER); // - 0 = id
		result.append(eqa.getQuestionId().getQuestionText()).append(DELIMITER); // - 1 = question
		result.append(eqa.getDescription()).append(DELIMITER); // - 2 = description
		result.append(eqa.getCategory1()).append(DELIMITER);  // - 4 = lang cod	and company's categories
		result.append(eqa.getCorrectAnswer()).append(DELIMITER); // - 5 = correct answer (letter or number)
		result.append(eqa.getNumberOfResponsesInThePicture()).append(DELIMITER); // - 6 = num answers on picture
		result.append(eqa.getMetaCategory()).append(DELIMITER); // - 7 = meta category 
		result.append(getAnswers(eqa));  // optionaly 8-12 = variants of answers or stubs for ProgrammingTasks               
		
		return result.toString();
	}
	
	protected String getAnswers(EntityQuestionAttributes eqa) {
		List<EntityAnswersText> anRes;
		StringBuilder outRes = new StringBuilder("");
		if(eqa.getQuestionAnswersList() != null){					
			anRes = eqa.getQuestionAnswersList();
			for(EntityAnswersText rRes :anRes){
				outRes.append(rRes.getAnswerText()).append(DELIMITER);
			}
		}
		return outRes.toString();
	}
	
	@Override
	public String[]  getQuestionById(String questionID, int actionKey) {// getting question attributes by ID !!!!!!!!!!!!!!!!!!!!!!!!!  
		String[] outArray = new String[4];
		StringBuilder  outRes = new StringBuilder("");
		StringBuilder outAnsRes = new StringBuilder("");	
		List<EntityAnswersText> answers;
		long id = (long)Integer.parseInt(questionID);
		EntityQuestionAttributes question = em.find(EntityQuestionAttributes.class, id);
		
				
		if(question != null){			
			if(ifAllowed(question)) {			
				answers = question.getQuestionAnswersList();
				String imageBase64Text=null;
				String fileLocation;
				
				////
				if((fileLocation = question.getFileLocationLink()) != null && question.getFileLocationLink().length() > 25 
						&& !question.getMetaCategory().equals(TestProcessor.MC_PROGRAMMING)){
					imageBase64Text = encodeImage(FileManagerService.BASE_DIR_IMAGES  + fileLocation);
					outArray[1] = "data:image/jpeg;base64," + imageBase64Text; // TO DO delete!!! hard code -- data:image/png;base64,
				}else{
					outArray[1] = null;
				}
				////
				
			switch(actionKey){
				case ACTION_GET_ARRAY: 
					outRes.append(question.getQuestionId().getQuestionText()).append(DELIMITER); // text of question or Description to picture or code									
					outRes.append(question.getCorrectAnswer()).append(DELIMITER); // correct answer char 
					outRes.append(question.getNumberOfResponsesInThePicture()).append(DELIMITER); // number of answers chars on image
					outRes.append(question.getMetaCategory());
					break;
					
				case ACTION_GET_FULL_ARRAY: 
					outRes.append(question.getQuestionId().getQuestionText()).append(DELIMITER);// text of question
					outRes.append(question.getDescription()).append(DELIMITER);  	// text of  Description 	
					outRes.append(question.getMetaCategory()).append(DELIMITER); // meta category 
					outRes.append(question.getCategory1()).append(DELIMITER); // language of sintax code in question
					outRes.append(question.getCategory2()).append(DELIMITER);// category of question
					outRes.append(question.getId()).append(DELIMITER);// static information
					outRes.append(question.getCorrectAnswer()).append(DELIMITER); // correct answer char 
					outRes.append(question.getNumberOfResponsesInThePicture()).append(DELIMITER);// number of answers chars on image					
					outRes.append(question.getLevelOfDifficulty());// level of difficulty for question
					break;
					
				default:System.out.println(" default switch");
			}		
			
			if(answers != null){
				for(EntityAnswersText tAn :answers) {
					outAnsRes.append(tAn.getAnswerText()).append(DELIMITER);	// answers on text if exist!!!
				}
				outArray[3] = outAnsRes.toString();
			}	
			outArray[0] = outRes.toString();// question attributes in text			
			outArray[2] = FileManagerService.BASE_DIR_IMAGES + fileLocation;// that static parameter for one operation!. when question is update from admin panel !!!
		}else{
			outArray[1] = " "; // TO DO delete!!! hard code -- data:image/png;base64,
			outArray[0] = "wrong request q.Id-"+questionID;// out text stub for tests 
		}		
		}
		return outArray ;// return to client 
	}
	
	abstract protected boolean ifAllowed(EntityQuestionAttributes question);
	
	protected String encodeImage(String imageLink) {	//method getting jpg file and converting to base64 for sending to FES 		
		String res = null;
		byte[] bytes = null;
		FileInputStream file;
		try {
			file = new FileInputStream(imageLink);
			bytes = new byte[file.available()];
			file.read(bytes);
			file.close();
			res = Base64.getEncoder().encodeToString(bytes);
		} catch (FileNotFoundException e) {	} 
		catch (IOException e) {
			System.out.println("file not found");//-------------------------------------------------------------------------sysout	
		} 
		catch (NullPointerException e) {
			System.out.println("Null Pointer Exception");//-----------------------------------------------------------------sysout	
		}
		return res;
	}
	
	@Override
	public List<String> getCategories1ByMetaCategory(String metaCategory) {		
		String query = "Select DISTINCT q.category1 FROM EntityQuestionAttributes q WHERE q.metaCategory='"		
				+ metaCategory + "'";
		String qry = getLimitsForQuery();
		if(qry!=null)
			query = query.concat(" AND q").concat(qry);
		
		query = query.concat(" ORDER BY q.category1");
		
		return getQuery(query);
		
	}
	
	@Override
	public List<String> getPossibleCategories1(String metaCategory) {
		return TestProcessor.getCategoriesList(metaCategory);
	}

	@Override
	public List<String> getPossibleMetaCaterories(){
		// ����� ����� (�����������) - TestProcessor.getMetaCategory() - ���������� ���� �������� � ��������� ����-���������
		return  TestProcessor.getMetaCategory();
	}

	@Override
	public List<String> getUsersCategories1FromDataBase() {
		StringBuilder query = new StringBuilder("Select DISTINCT q.category1 FROM EntityQuestionAttributes q WHERE (q.metaCategory='");
		query.append(IPublicStrings.COMPANY_AMERICAN_TEST).append("' OR q.metaCategory='").append(IPublicStrings.COMPANY_QUESTION).
			append("') AND q.category1 is not null");
		String str = getLimitsForQuery();
		if(str!=null)
			query.append(" AND q.").append(str);
		query.append(" ORDER BY q.category1");	
		
				
		return getQuery(query.toString());
	}
	
	@Override
	public String getJsonQuestionById(long id) {
		EntityQuestionAttributes eqa = em.find(EntityQuestionAttributes.class, id);
		String result = null;
		if(eqa!=null) {
			try {
				JSONObject jsn = getPartOfJSON(eqa);
				jsn.put(ICommonData.JSN_CORRECT_ANSWER_CHAR, eqa.getCorrectAnswer());
				jsn.put(ICommonData.JSN_QUESTION_TEXT, eqa.getQuestionId().getQuestionText());
				jsn.put(ICommonData.JSN_ANSWERS_NUMBER, eqa.getNumberOfResponsesInThePicture());
				jsn.put(ICommonData.JSN_QUESTION_DESCRIPTION, eqa.getDescription());
				
				if(eqa.getMetaCategory().equals(TestProcessor.MC_PROGRAMMING)) {
					jsn.put(ICommonData.JSN_CODE_SIMPLE, eqa.getAnswers().get(0));
				} else {
					JSONArray array = new JSONArray();
					List<String> answers = eqa.getAnswers();
					array.put(answers);
					jsn.put(ICommonData.JSN_ANSWER_OPTIONS, array);					
				}	
				
				String path = (eqa.getFileLocationLink());
				if(path!=null)
					jsn.put(ICommonData.JSN_IMAGE, FileManagerService.getImageForTests(path));
				
				result = jsn.toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}		
		
		System.out.println(result); // ------------------------------------------------- SYSO ------ !!!!!!!!!!!!!!
		return result;		
	}
	
	protected JSONObject getPartOfJSON(EntityQuestionAttributes eqa) throws JSONException {
		JSONObject jsn = null;
		if(eqa!=null) {
			jsn = new JSONObject();
			jsn.put(ICommonData.JSN_QUESTION_ID, eqa.getId());
			jsn.put(ICommonData.JSN_META_CATEGORY, eqa.getMetaCategory());
			jsn.put(ICommonData.JSN_CATEGORY1, eqa.getCategory1());
			jsn.put(ICommonData.JSN_CATEGORY2, eqa.getCategory2());	
			jsn.put(ICommonData.JSN_DIFFICULTY_LVL, eqa.getLevelOfDifficulty());
			if(eqa.getFileLocationLink()!=null) {
				jsn.put(ICommonData.JSN_IS_IMAGE, true);				
			} else {
				jsn.put(ICommonData.JSN_IS_IMAGE, false);	
			}
		} 
		return jsn;
	}
	
	@Transactional(readOnly=false, propagation = Propagation.REQUIRED)
	protected JSONObject getStatusOfTest(long testId) throws JSONException {
		int correct = 0;
		int inCorrect = 0;
		int unAnswered = 0;
		int unChecked = 0;
		JSONArray arrayUnchecked = new JSONArray();
		
		EntityTest test = em.find(EntityTest.class, testId);
		
		String query = "SELECT c from EntityTestQuestions c WHERE c.entityTest=?1";
		List<EntityTestQuestions> list = em.createQuery(query).setParameter(1, test).getResultList();		
		
				
		for(EntityTestQuestions etq : list) {
			int status = etq.getStatus();
			
			switch(status) {
			case ICommonData.STATUS_CORRECT :
				correct++;
				break;
			case ICommonData.STATUS_INCORRECT :
				inCorrect++;
				break;
			case ICommonData.STATUS_NO_ANSWER :
				unAnswered++;
				break;
			case ICommonData.STATUS_UNCHECKED :
				unChecked++;
				JSONObject jsn = new JSONObject();
				jsn.put(ICommonData.JSN_TEST_QUESTION_ID, etq.getId());				
				break;
			}
		}
		
		boolean testIsPassed;
		boolean testIsChecked;
		
				
		if(unAnswered==0) {			
			testIsPassed = true;
			if(!test.isPassed()) {				
				test.setPassed(true);
				em.merge(test);				
			}
		} else {
			testIsPassed = false;
		}
		
		if(unChecked==0) {
			testIsChecked = true;
			if(!test.isChecked()) {
				test.setChecked(true);
				em.merge(test);
			}
		} else {
			testIsChecked = false;
		}
		
				
		if(test.getAmountOfCorrectAnswers()!=correct) {
			test.setAmountOfCorrectAnswers(correct);
			em.merge(test);
		}		
		int allQuestions = test.getAmountOfQuestions();
			
		JSONObject result = new JSONObject();
		result.put(ICommonData.JSN_TEST_CORRECT_ANSWERS, correct);
		result.put(ICommonData.JSN_TEST_INCORRECT_ANSWERS, inCorrect);
		result.put(ICommonData.JSN_TEST_IS_CHECKED, testIsChecked);
		result.put(ICommonData.JSN_TEST_IS_PASSED, testIsPassed);
		result.put(ICommonData.JSN_TEST_LIST_ANSWERS_TO_CHECK, arrayUnchecked);
		result.put(ICommonData.JSN_TEST_QUESTION_NUMBER, allQuestions);
		result.put(ICommonData.JSN_TEST_UNANSWERED_ANSWERS, unAnswered);
		result.put(ICommonData.JSN_TEST_UNCHECKED_ANSWERS, unChecked);
		
		float percent;
		int checked = allQuestions - unAnswered - unChecked;
		if(checked==0) 
			percent = 0;
		else
			percent = Math.round((float)correct/(float)checked*100);
		
		if(unChecked==0 && unAnswered==0) {
			result.put("persentOfRightAnswers",Float.toString(percent)); // Add calculations from the resultTestCodeFromPerson field
		} else if (unChecked==0){
			result.put("persentOfRightAnswers", "not checked");
		}
		
		return result;
	}

}
