sap.ui.define(
  [
    './BaseController',
    'sap/ui/model/json/JSONModel',
    'sap/ui/core/routing/History',
    '../model/formatter',
    'sap/ui/core/Fragment'
  ],
  function (BaseController, JSONModel, History, formatter, Fragment) {
    'use strict'
    var sObjectId
    return BaseController.extend('proj.Projectapp.controller.Object', {
      formatter: formatter,
      /* =========================================================== */
      /* lifecycle methods                                           */
      /* =========================================================== */
      /**
       * Called when the worklist controller is instantiated.
       * @public
       */
      onInit: function () {
        // Model used to manipulate control states. The chosen values make sure,
        // detail page shows busy indication immediately so there is no break in
        // between the busy indication for loading the view's meta data
        var oViewModel = new JSONModel({
          busy: true,
          delay: 0
        })
        this.getRouter()
          .getRoute('object')
          .attachPatternMatched(this._onObjectMatched, this)
        this.setModel(oViewModel, 'objectView') //this.setModel(oViewModel, "objectViewa");
      },
      /* =========================================================== */
      /* event handlers                                              */
      /* =========================================================== */
      /**
       * Event handler when the share in JAM button has been clicked
       * @public
       */
      onShareInJamPress: function () {
        var oViewModel = this.getModel('objectView'),
          oShareDialog = sap.ui.getCore().createComponent({
            name: 'sap.collaboration.components.fiori.sharing.dialog',
            settings: {
              object: {
                id: location.href,
                share: oViewModel.getProperty('/shareOnJamTitle')
              }
            }
          })
        oShareDialog.open()
      },
      /* =========================================================== */
      /* internal methods                                            */
      /* =========================================================== */
      /**
       * Binds the view to the object path.
       * @function
       * @param {sap.ui.base.Event} oEvent pattern match event in route 'object'
       * @private
       */
      _onObjectMatched: function (oEvent) {
        sObjectId = oEvent.getParameter('arguments').objectId
        this._bindView('/Project' + sObjectId)
        var oProjectsModel = new JSONModel(
          '/srv_api/catalog/Project' + sObjectId + '/employees',
          true
        )
        this.getView().setModel(oProjectsModel, 'empprojects')
        this.getView()
          .getModel('empprojects')
          .setProperty('/busy', true)
        this.getView()
          .getModel('empprojects')
          .setProperty('/delay', 2)
        this.getView()
          .getModel('objectView')
          .setProperty('/busy', false)
      },
      /**
       * Binds the view to the object path.
       * @function
       * @param {string} sObjectPath path to the object to be bound
       * @private
       */
      _bindView: function (sObjectPath) {
        var oViewModel = this.getModel('objectView')
        this.getView().bindElement({
          path: sObjectPath,
          events: {
            change: this._onBindingChange.bind(this),
            dataRequested: function () {
              oViewModel.setProperty('/busy', true)
            },
            dataReceived: function () {
              oViewModel.setProperty('/busy', false)
            }
          }
        })
      },
      _onBindingChange: function () {
        var oView = this.getView(),
          oViewModel = this.getModel('objectView'),
          oElementBinding = oView.getElementBinding()
        // No data for the binding
        if (!oElementBinding.getBoundContext()) {
          this.getRouter()
            .getTargets()
            .display('objectNotFound')
          return
        }
        var oResourceBundle = this.getResourceBundle()
        oView
          .getBindingContext()
          .requestObject()
          .then(
            function (oObject) {
              var sObjectId = oObject.ID,
                sObjectName = oObject.projectName
              // Add the object page to the flp routing history
              // this.addHistoryEntry({
              // 	title: this.getResourceBundle().getText("objectTitle") + " - " + sObjectName,
              // 	icon: "sap-icon://enter-more",
              // 	intent: "#Projects-display&/Project(" + sObjectId + ")"
              // });
              oViewModel.setProperty('/busy', false)
              oViewModel.setProperty(
                '/saveAsTileTitle',
                oResourceBundle.getText('saveAsTileTitle', [sObjectName])
              )
              oViewModel.setProperty('/shareOnJamTitle', sObjectName)
              oViewModel.setProperty(
                '/shareSendEmailSubject',
                oResourceBundle.getText('shareSendEmailObjectSubject', [
                  sObjectId
                ])
              )
              oViewModel.setProperty(
                '/shareSendEmailMessage',
                oResourceBundle.getText('shareSendEmailObjectMessage', [
                  sObjectName,
                  sObjectId,
                  location.href
                ])
              )
            }.bind(this)
          )
      },
      /**
       *@memberOf proj.Projectapp.controller.Object
       */
      assignemployee: function (oEvent) {
        var oButton = oEvent.getSource()
        var oEmployeeModel = new JSONModel('/srv_api/catalog/LoggedInUser')
        this.getView().setModel(oEmployeeModel, 'user')
        var that = this
        oEmployeeModel.attachRequestCompleted(function () {
          console.log(oEmployeeModel.getData())
          var id = oEmployeeModel.getData()
          var mmagerid = id.value[0].id
          var oEmployeeModelset = new JSONModel(
            "/odata/v2/User('" +
              mmagerid +
              "')/directReports?$format=json&$select=userId,defaultFullName"
          )
          that.getView().setModel(oEmployeeModelset, 'employees')
          that
            .getView()
            .getModel('employees')
            .setProperty('/busy', true)
          that
            .getView()
            .getModel('employees')
            .setProperty('/delay', 2)
          that
            .getView()
            .getModel('objectView')
            .setProperty('/busy', false)
        })

        if (!this._oPopover) {
          Fragment.load({
            id: 'popoverNavCon',
            name: 'proj.Projectapp.view.createproject',
            controller: this
          }).then(
            function (oPopover) {
              this._oPopover = oPopover
              this.getView().addDependent(this._oPopover)
              this._oPopover.openBy(oButton)
            }.bind(this)
          )
        } else {
          this._oPopover.openBy(oButton)
        }
      },
      onNavToProduct: function (oEvent) {
        var oButton = oEvent
          .getSource()
          .getBindingContext()
          .getValue('ID')
        //var projectid = oButton.split("/Project").slice(-1).pop();
        var empid = oEvent.getSource().getTitle()
        var oTable = this.getView().byId('list0')
        var path = oEvent.getSource().getBindingContext().sPath
        var olist = this.getView()
          .byId('list0')
          .getBinding('items')
        var data = {
          project_ID: oButton,
          employeeId: empid
        }
        var prjName = oEvent
          .getSource()
          .getBindingContext()
          .getValue('projectName')
        var oView = this.getView()
        var that = this
        $.ajax({
          type: 'POST',
          async: true,
          contentType: 'application/json; charset=utf-8',
          url: '/srv_api/catalog/Mappings',
          data: JSON.stringify(data),
          success: function (responsedata) {
            sap.m.MessageBox.success(
              'Employee ' +
                responsedata.employeeId +
                ' is assigned to the Project ' +
                prjName
            )
            that._oPopover.close()
            var oViewModel = new JSONModel({
              busy: true,
              delay: 0
            })
            that.setModel(oViewModel, 'employ')
            oTable.getModel('empprojects').refresh(true)
            var updatedModel = new sap.ui.model.json.JSONModel(
              '/srv_api/catalog' + path + '/employees'
            )
            oView.setModel(updatedModel, 'empprojects')
            that
              .getView()
              .getModel('empprojects')
              .setProperty('/busy', true)
            that
              .getView()
              .getModel('empprojects')
              .setProperty('/delay', 2)
            that
              .getView()
              .getModel('objectView')
              .setProperty('/busy', false)
          },
          error: function (error) {
            if (error.responseJSON.error.code === '400') {
              sap.m.MessageBox.error(
                'Employee ' + empid + ' is already assigned to this Project'
              )
            } else {
              sap.m.MessageBox.warning('Session Expired')
              that._oPopover.close()
            }
          }
        })
      }
    })
  }
)
