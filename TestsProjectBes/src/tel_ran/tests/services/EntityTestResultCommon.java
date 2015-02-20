package tel_ran.tests.services;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import tel_ran.tests.services.EntityTestResultDetails;

import javax.persistence.Embedded;

import tel_ran.tests.services.EntityPerson;
import tel_ran.tests.services.common.CommonData;

import javax.persistence.ManyToOne;

import org.json.simple.JSONObject;

@Entity
@Table(name="TEST_RESULTS")
public class EntityTestResultCommon {
	@Id
	@GeneratedValue
	private int testID;
	private String testCategory = "";
	private String testName = "";
	private Date testDate;
	
	@Embedded
	private EntityTestResultDetails entityTestDetails;
	@ManyToOne
	@JoinColumn(name="personID")
	private EntityPerson entityPerson;
	
	public EntityTestResultCommon() {
		
	}
	public int getTestID() {
		return testID;
	}
	public void setTestID(int testID) {
		this.testID = testID;
	}
	public String getTestCategory() {
		return testCategory;
	}
	public void setTestCategory(String testCategory) {
		this.testCategory = testCategory;
	}
	public String getTestName() {
		return testName;
	}
	public void setTestName(String testName) {
		this.testName = testName;
	}
	public Date getTestDate() {
		return testDate;
	}
	public void setTestDate(Date testDate) {
		this.testDate = testDate;
	}
	
/*	@Override
	public String toString() {
		StringBuffer strbuf = new StringBuffer();
		strbuf.append(testID);
		strbuf.append(CommonData.delimiter);
		strbuf.append(testCategory);
		strbuf.append(CommonData.delimiter);
		strbuf.append(testName);
		strbuf.append(CommonData.delimiter);
		strbuf.append(testDate);
		strbuf.append(CommonData.delimiter);
		strbuf.append(entityTestDetails);
		return strbuf.toString();
		return this.toJson().toJSONString();
	}*/

public JSONObject toJson()
	{
		JSONObject json = new JSONObject();
		json.put("testid",testID);
		json.put("testCategory",testCategory);
		json.put("testName", testName);
		json.put("testDate", testDate);
		json.put("entityPerson", entityPerson);
		json.put("entityTestDetails", entityTestDetails);
		System.out.println(json);
		return json;
	}
	public EntityTestResultDetails getEntityTestDetails() {
	    return entityTestDetails;
	}
	public void setEntityTestDetails(EntityTestResultDetails param) {
	    this.entityTestDetails = param;
	}
	public EntityPerson getEntityPerson() {
	    return entityPerson;
	}
	public void setEntityPerson(EntityPerson param) {
	    this.entityPerson = param;
	}

}
