sap.ui.define([
	"sap/ui/model/json/JSONModel",
	"sap/ui/core/mvc/Controller",
	"sap/m/MessageToast",
	"sap/ui/model/Filter"
], function (JSONModel, Controller, MessageToast, Filter) {
	"use strict";

	return Controller.extend("sap.ui.demo.myapp.controller.Employee", {
		onInit: function () {
			this.oOwnerComponent = this.getOwnerComponent();

			this.oRouter = this.oOwnerComponent.getRouter();
			this.oModel = this.oOwnerComponent.getModel();

			this.oRouter.getRoute("employee").attachPatternMatched(this._onPatternMatch, this);
		},

		_onPatternMatch: function (oEvent) {
			this._employee = oEvent.getParameter("arguments").employee || this._employee || "0";
			this._project = oEvent.getParameter("arguments").project || this._project || "0";

			this.getView().bindElement({
				path: "/" + this._project + "/employees/" + this._employee,
				model: "projects"
			});

			var empId = this.getView().getModel("projects").getData()[this._project]["employees"][this._employee]["employeeId"];
			var oProjectsModel = new JSONModel('/sf-extension.svc/api/v1/employees/' + empId);
			this.getView().setModel(oProjectsModel, 'empProjects');

			var oSkillsModel = new JSONModel("/odata/v2/SkillProfile(externalCode='"+empId+"')?&$format=json&$expand=externalCodeNav,ratedSkills/skillNav&$select=ratedSkills/skillNav/name_en_US");
			this.getView().setModel(oSkillsModel, 'empSkills');

		},

		handleFullScreen: function () {
			var sNextLayout = this.oModel.getProperty("/actionButtonsInfo/endColumn/fullScreen");
			this.oRouter.navTo("employee", {
				layout: sNextLayout,
				project: this._project,
				employee: this._employee
			});
		},

		handleExitFullScreen: function () {
			var sNextLayout = this.oModel.getProperty("/actionButtonsInfo/endColumn/exitFullScreen");
			this.oRouter.navTo("employee", {
				layout: sNextLayout,
				project: this._project,
				employee: this._employee
			});
		},

		handleClose: function () {
			var sNextLayout = this.oModel.getProperty("/actionButtonsInfo/endColumn/closeColumn");
			this.oRouter.navTo("detail", {
				layout: sNextLayout,
				project: this._project
			});
		},

		handleDialogClose: function (oEvent) {
			var aContexts = oEvent.getParameter("selectedContexts");
			if (aContexts && aContexts.length) {
				MessageToast.show("You have chosen " + aContexts.map(function (oContext) {
					return oContext.getObject().projectName;
				}).join(", "));
			} else {
				MessageToast.show("No new item was selected.");
			}
			oEvent.getSource().getBinding("items").filter([]);
		},

		handleSearch: function (oEvent) {
			var sValue = oEvent.getParameter("value");
			var oFilter = new Filter("projectName", sap.ui.model.FilterOperator.Contains, sValue);
			var oBinding = oEvent.getSource().getBinding("items");
			oBinding.filter([oFilter]);
		},

		handleLinkProject: function (oEvent) {

			var oProjectsModel = new JSONModel( '/sf-extension.svc/api/v1/projects')
			oProjectsModel.setSizeLimit(1000);

			if (!this._oDialog) {
				this._oDialog = sap.ui.xmlfragment("sap.ui.demo.myapp.Fragments.projectDialog", this);
				this._oDialog.setModel(oProjectsModel, 'allprojects');
			}

			// toggle compact style
			jQuery.sap.syncStyleClass("sapUiSizeCompact", this.getView(), this._oDialog);
			this._oDialog.open();

		},

		onExit: function () {
			if (this._oDialog) {
				this._oDialog.destroy();
			}
			this.oRouter.getRoute("employee").detachPatternMatched(this._onPatternMatch, this);
		}
	});
});