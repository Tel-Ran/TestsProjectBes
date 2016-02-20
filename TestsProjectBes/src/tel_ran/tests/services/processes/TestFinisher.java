package tel_ran.tests.services.processes;

import java.util.Collection;
import java.util.HashMap;

import java.util.List;
import java.util.Map;


import org.json.JSONException;
import org.springframework.stereotype.Service;
import tel_ran.tests.dao.IDataTestsQuestions;
import tel_ran.tests.entitys.Test;
import tel_ran.tests.entitys.InTestQuestion;
import tel_ran.tests.services.common.ICommonData;
import tel_ran.tests.services.subtype_handlers.ITestQuestionHandler;


@Service
public class TestFinisher implements ITestProcess {
	
	
	private Test test;
	private IDataTestsQuestions dao;
	
	public TestFinisher() {
		
	}
	
	public void setTest(Test test) {
		this.test = test;
	}


	@Override	
	public void run() {	
		
		long time = System.currentTimeMillis();
		test.setEndTestDate(time);	
		test.setDuration((int)(time - test.getStartTestDate()));			
		
		List<InTestQuestion> list = test.getInTestQuestions();		
		
		Map<Integer, CategoryResults> catResults = saveAnswers(list);		
		
		
		try {			
			saveStatus(catResults);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
			
		dao.saveTest(test);
		
		
	}
	
	

	private Map<Integer, CategoryResults> saveAnswers(List<InTestQuestion> list) {
		
		Map<Integer, CategoryResults> catResults = new HashMap<Integer, CategoryResults>();
		
		for(InTestQuestion tQuestion : list) {
			ITestQuestionHandler tHandler = tQuestion.getQuestion().getHandler();
			tHandler.setCompanyId(test.getCompany().getId());
			tHandler.setTestId(test.getId());
			tHandler.setTestQuestion(tQuestion);
			int res = tHandler.checkResult();
			
			
			
			int categoryId = tQuestion.getQuestion().getCategory().getId();
			
			
			if(catResults.containsKey(categoryId)) {
				CategoryResults catResult = catResults.get(categoryId);
				catResult.setAnswerResult(res);
				
			} else {
				CategoryResults catResult = new CategoryResults(categoryId, tHandler.getCategoryType());
				catResult.setAnswerResult(res);
				catResults.put(categoryId, catResult);
			}
			tQuestion.setStatus(res);
			dao.saveAnswer(tQuestion);
						
		}	
		
		return catResults;
		
	}
	
	
	private boolean saveStatus(Map<Integer, CategoryResults> resultsByCategory) throws JSONException {
		int status[] = new int[4];
		int answers = 0;
		int questions = 0;
		
		Collection<CategoryResults> results = resultsByCategory.values();
		
		for(CategoryResults res : results) {
			status[ICommonData.STATUS_CORRECT] += res.getNumCorrectAnswers();
			answers += res.getNumAnswers();
			status[ICommonData.STATUS_INCORRECT] += answers - res.getNumCorrectAnswers();
			status[ICommonData.STATUS_UNCHECKED] += res.getNumUncheckedUnswers();
			questions += res.getNumQuestions();			
		}
		
		status[ICommonData.STATUS_UNCHECKED] = questions - answers;
		
	
		
		if(status[ICommonData.STATUS_NO_ANSWER] == 0) {
			test.setPassed(true);			
		}
		
		if(status[ICommonData.STATUS_UNCHECKED]==0 && status[ICommonData.STATUS_NO_ANSWER] ==0) {
			test.setChecked(true);			
		}
		
		test.setAmountOfCorrectAnswers(status[ICommonData.STATUS_CORRECT]);
		
		test.createJsonResult(results, answers);
		
		return true;
		
	}

	public void setDao(IDataTestsQuestions testQuestsionsData) {
		this.dao = testQuestsionsData;
		
	}
	


}
