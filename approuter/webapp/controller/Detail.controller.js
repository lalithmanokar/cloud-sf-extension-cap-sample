sap.ui.define([
	"sap/ui/core/mvc/Controller"
], function (Controller) {
	"use strict";

	return Controller.extend("sap.ui.demo.myapp.controller.Detail", {
		onInit: function () {
			this.oOwnerComponent = this.getOwnerComponent();

			this.oRouter = this.oOwnerComponent.getRouter();
			this.oModel = this.oOwnerComponent.getModel();

			this.oRouter.getRoute("master").attachPatternMatched(this._onProjectMatched, this);
			this.oRouter.getRoute("detail").attachPatternMatched(this._onProjectMatched, this);
			this.oRouter.getRoute("employee").attachPatternMatched(this._onProjectMatched, this);
		},

		_onProjectMatched: function (oEvent) {
			this._project = oEvent.getParameter("arguments").project || this._project || "0";
			this.getView().bindElement({
				path: "/" + this._project,
				model: "projects"
			});
		},

		onEditToggleButtonPress: function() {
			var oObjectPage = this.getView().byId("ObjectPageLayout"),
				bCurrentShowFooterState = oObjectPage.getShowFooter();

			oObjectPage.setShowFooter(!bCurrentShowFooterState);
		},
		
		onEmployeePress: function (oEvent) {
			var employeePath = oEvent.getSource().getBindingContext("projects").getPath(),
				employee = employeePath.split("/").slice(-1).pop(), oNextUIState;

			//this.oRouter.navTo("employee", {layout: fioriLibrary.LayoutType.ThreeColumnsMidExpanded, employee: employee, project: this._project});
			
			this.oOwnerComponent.getHelper().then(function (oHelper) {
				oNextUIState = oHelper.getNextUIState(2);
				this.oRouter.navTo("employee", {
					layout: oNextUIState.layout,
					employee: employee,
					project: this._project
				});
			}.bind(this));
		},
		
		handleFullScreen: function () {
			var sNextLayout = this.oModel.getProperty("/actionButtonsInfo/midColumn/fullScreen");
			this.oRouter.navTo("detail", {layout: sNextLayout, project: this._project});
		},

		handleExitFullScreen: function () {
			var sNextLayout = this.oModel.getProperty("/actionButtonsInfo/midColumn/exitFullScreen");
			this.oRouter.navTo("detail", {layout: sNextLayout, project: this._project});
		},

		handleClose: function () {
			var sNextLayout = this.oModel.getProperty("/actionButtonsInfo/midColumn/closeColumn");
			this.oRouter.navTo("master", {layout: sNextLayout});
		},

		onExit: function () {
			this.oRouter.getRoute("master").detachPatternMatched(this._onProjectMatched, this);
			this.oRouter.getRoute("detail").detachPatternMatched(this._onProjectMatched, this);
		}
	});
});