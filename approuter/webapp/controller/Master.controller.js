sap.ui.define([
	"sap/ui/model/json/JSONModel",
	"sap/ui/core/mvc/Controller",
	"sap/ui/model/Filter",
	"sap/ui/model/FilterOperator",
	'sap/ui/model/Sorter',
	"sap/m/MessageBox",
	"sap/ui/core/Fragment",
], function (JSONModel, Controller, Filter, FilterOperator, Sorter, MessageBox, Fragment) {
	"use strict";

	return Controller.extend("sap.ui.demo.myapp.controller.Master", {
		onInit: function () {
			this.oView = this.getView();
			this._bDescendingSort = false;
			this.oProjectsTable = this.oView.byId("projectsTable");
			this.oRouter = this.getOwnerComponent().getRouter();
		},

		onSearch: function (oEvent) {
			var oTableSearchState = [],
				sQuery = oEvent.getParameter("query");

			if (sQuery && sQuery.length > 0) {
				oTableSearchState = [new Filter("Name", FilterOperator.Contains, sQuery)];
			}

			this.oProjectsTable.getBinding("items").filter(oTableSearchState, "Application");
		},

		onAddNewProject: function () {
			//MessageBox.information("This functionality is not ready yet.", {title: "Aw, Snap!"});
			//this.oRouter.navTo("newProject", {layout: fioriLibrary.LayoutType.EndColumnFullScreen});
			
			var oNextUIState;
			this.getOwnerComponent().getHelper().then(function (oHelper) {
				oNextUIState = oHelper.getNextUIState(3);
				this.oRouter.navTo("newProject", {layout: oNextUIState.layout});
			}.bind(this));
		},
		
		onNotificationPopover: function (oEvent) {
			
			var oNotificationModel = new JSONModel( '/sf-extension.svc/api/v1/notificationsForManager/106010');
			oNotificationModel.setSizeLimit(1000);

			// create popover
			if (!this._oPopover) {
				this._oPopover = sap.ui.xmlfragment("popoverNavCon", "sap.ui.demo.myapp.Fragments.notification", this);
				this.getView().addDependent(this._oPopover);
			}
			
			this.getView().setModel(oNotificationModel, "notifications");

			this._oPopover.openBy(oEvent.getSource());
		},
		
		onNavToSummary : function (oEvent) {
			var oCtx = oEvent.getSource().getBindingContext("notifications");
			var oNavCon = Fragment.byId("popoverNavCon", "navCon");
			var oDetailPage = Fragment.byId("popoverNavCon", "detail");
			oNavCon.to(oDetailPage);
			oDetailPage.bindElement({
				path:oCtx.getPath(),
				model: 'notifications'
			});

			var notifyPath= oEvent.getSource().getBindingContext("notifications").getPath();
			var notify= notifyPath.split("/").slice(-1).pop();
			var empId= this.getView().getModel("notifications").getData()[notify];
			var oprofileModel = new JSONModel( '/sf-extension.svc/api/v1/employees/' + empId.employeeId);

			this.getView().setModel(oprofileModel, 'empProfile');

			var oSkillsModel = new JSONModel("/odata/v2/SkillProfile(externalCode='"+empId.employeeId+"')?&$format=json&$expand=externalCodeNav,ratedSkills/skillNav&$select=ratedSkills/skillNav/name_en_US");
			this.getView().setModel(oSkillsModel, 'notifyEmpSkills');
		},
		
		onNavBackPopup: function(){
			var oNavCon = Fragment.byId("popoverNavCon", "navCon");
			oNavCon.back();
		},

		onSort: function () {
			this._bDescendingSort = !this._bDescendingSort;
			var oBinding = this.oProjectsTable.getBinding("items"),
				oSorter = new Sorter("projectName", this._bDescendingSort);

			oBinding.sort(oSorter);
		},
		
		onListItemPress: function (oEvent) {
			var projectPath = oEvent.getSource().getBindingContext("projects").getPath(),
				project = projectPath.split("/").slice(-1).pop(), oNextUIState;

			//this.oRouter.navTo("detail", {layout: fioriLibrary.LayoutType.TwoColumnsMidExpanded, project: project});
			this.getOwnerComponent().getHelper().then(function (oHelper) {
				oNextUIState = oHelper.getNextUIState(1);
				this.oRouter.navTo("detail", {
					layout: oNextUIState.layout,
					project: project 
				});
			}.bind(this));
		},

		productCount: function(oValue) {
		    //return the number of products linked to Category // sync call only to get $count
		    if (oValue) {
		        var sPath = this.getBindingContext().getPath() + '/Products';
		        var oBindings = this.getModel().bindList(sPath);
		        return oBindings.getLength();
		    }
		}
	});
});