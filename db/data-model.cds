namespace sfextension.refapp;
using { cuid } from '@sap/cds/common';

entity Project: cuid {
  projectName  : String;
  description  : String;
  employees: Association to many EmployeeProjectMapping on employees.project = $self;
}

entity EmployeeProjectMapping{
	key project: Association to Project;
	key employeeId: String;
  @cds.persistence.skip
  employeeName:String;
}


entity Notifications:cuid {
  message: String;
  employeeId: String;
  managerId: String;
  readStatus: Boolean;
  @cds.persistence.skip
  employeeName:String;
  @cds.persistence.skip
  skills: String;
}
