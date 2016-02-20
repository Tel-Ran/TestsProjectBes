package tel_ran.tests.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import tel_ran.tests.dao.IDataTestsQuestions;
import tel_ran.tests.entitys.InTestQuestion;
import tel_ran.tests.entitys.Question;
import tel_ran.tests.entitys.Test;
import tel_ran.tests.services.common.ICommonData;
import tel_ran.tests.services.processes.TestFinisher;
import tel_ran.tests.services.subtype_handlers.ITestQuestionHandler;
import tel_ran.tests.services.utils.FileManagerService;
import tel_ran.tests.utils.errors.DataException;

@Component("testService")
@Scope("prototype")
public class TestService extends AbstractService {
	
	@Autowired
	IDataTestsQuestions testQuestsionsData;	
	
	long testId;
	Test test;
	
	/**
	 * TaskExecutor is used for the process of test finishing (analyzing in a thread)
	 */
	@Autowired
	TaskExecutor taskExecutor;
	
	
	public long getTestId() {
		return testId;
	}

	public void setTestId(long testId) throws DataException {
		this.testId = testId;
		this.test = testQuestsionsData.findTestById(testId);
		if(this.test==null) throw new DataException(DataException.NO_TEST);
	}

	@Override
	public String getAllElements() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createNewElement(String dataJson) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getSimpleList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInformation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getElement(String answer) {
			
		String result = null;
		
		if(!test.isPassed()) {
			
			try {
				long gotAnswer = this.saveAnswer(answer);
			
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return passedTestMessage();
			}			
			
			checkTestStartTime();
			
			List<InTestQuestion> activeQuestions = getActiveQuestions();		
						
			if(activeQuestions.size()>0) {
				try {
					result = getNewQuestion(activeQuestions);
				} catch (JSONException e) {
					e.printStackTrace();
				} 				
				
			} else {	
				//if the test is finished (no questions in the response from DB), 
				//TestFinisher will be start in a new Thread
				result = passedTestMessage();
				TestFinisher finisher = new TestFinisher();
				finisher.setTest(test);
				finisher.setDao(this.testQuestsionsData);
				taskExecutor.execute(finisher);
			}
						
		}		
		
		
		
		return result;
	
	}
	
	private String passedTestMessage() {
		return "{\"error\":\"test is already passed\",\"isPassed\":true}";
		
	}
	
	
	private String getNewQuestion(List<InTestQuestion> activeQuestions) throws JSONException {
		
		
		int index = test.getAmountOfQuestions() - activeQuestions.size();		
		
		InTestQuestion tQuestion = activeQuestions.get(0);		
		Question baseQuestion = this.testQuestsionsData.initiateQuestionInTest(tQuestion);
		ITestQuestionHandler handler = baseQuestion.getHandler();			
		handler.setTestQuestion(tQuestion);
		
		return handler.getJsonForTest(index);			
	}

	private List<InTestQuestion> getActiveQuestions() {
		this.test = testQuestsionsData.findTestById(testId);
		
		List<InTestQuestion> allQuestions = this.test.getInTestQuestions();
		List<InTestQuestion> activeQuestions = new ArrayList<InTestQuestion>();
		for(InTestQuestion tQuestion : allQuestions) {
			if(tQuestion.getStatus()==ICommonData.STATUS_NO_ANSWER) {
				activeQuestions.add(tQuestion);
			}
		}		
		return activeQuestions;
	}

	private void checkTestStartTime() {
		if (test.getStartTestDate()==0) {
			test.setStartTestDate(System.currentTimeMillis());
			FileManagerService.initializeTestFileStructure(test.getCompany().getId(), test.getId());
			this.testQuestsionsData.saveTest(test);		
		}		
	}

	private long saveAnswer(String answer) throws JSONException {	
		long tQuestionId = -1;
		if(answer==null || answer.length()<3) return -1;
		
		System.out.println(answer);
				
		JSONObject jsn = new JSONObject(answer);
		
		if(jsn.has(ICommonData.JSN_INTEST_QUESTION_ID)) {
			tQuestionId = jsn.getLong(ICommonData.JSN_INTEST_QUESTION_ID);
						
			InTestQuestion tQuestion = testQuestsionsData.findTestQuestionById(tQuestionId);
			System.out.println(tQuestion.getId());
			System.out.println("STATUS = " + tQuestion.getStatus());
			
			if(tQuestion!=null) {
				
			if(tQuestion.getStatus()==ICommonData.STATUS_NO_ANSWER) {
				System.out.println("HERE");
				Question baseQuestion = tQuestion.getQuestion();
				ITestQuestionHandler testQuestionHandler = baseQuestion.getHandler();	
			
				try {
					tQuestion = testQuestionHandler.setPersonAnswer(jsn, tQuestion);
				
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
				if(tQuestion!=null) {
					testQuestsionsData.saveAnswer(tQuestion);
				}
			}
			}
		}
		return tQuestionId;
		
	}

	public void saveImage(String image) {		
		if(test!=null && !test.isPassed()){
			try {
					FileManagerService.saveImage(test.getCompany().getId(), testId, image);
			} catch (IOException e) {					
					e.printStackTrace();
			}			
		}
	}


}
