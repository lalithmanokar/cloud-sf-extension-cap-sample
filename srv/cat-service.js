const cds = require('@sap/cds')
module.exports = async srv => {
	const sf = await cds.connect.to('successFactorsService')
	const myMessaging = await cds.connect.to('myMessaging')
	const {
		Notifications
	} = srv.entities

	/* enterprise messaging Queue Name is sfemessage,
	    errors are logged automatically
	    If you are Using Enterprise Messaging Service Plan Default Replace the Line Number 13 with line number 12
	    myMessaging.on('sfemessage', msg => { */
	myMessaging.on('topic:sfemessage', msg => {
		const {
			message,
			employeeId,
			managerId,
			readStatus
		} = msg.headers
		return cds.run(
			INSERT.into(Notifications).entries({
				message,
				employeeId,
				managerId,
				readStatus
			})
		)
	})

	srv.on('READ', 'LoggedInUser', req => [{
		id: req.user.id,
		name: req.user.name.givenName
	}])

	//Reading Employee Name and Skill set of an employee using employeeId
	srv.after('READ', 'Notifications', (notifications, req) => {
			if (!req.user) return req.reject('Session Expired')
            if (notifications.some(count => typeof count.counted !== 'undefined')) {
				return
			} else {
				if (notifications.length == 0) {
					return
				} else {
					const tx = sf.transaction(req)
					return Promise.all(
						notifications
						.filter(notification => notification.employeeId)
						.map(notification =>
							Promise.all([
								getEmployeeName(notification, tx),
								getSkills(notification, tx)
							])
						)
					)
				}
			}
		})
		//Reading Employee Name using employeeId
	srv.after('READ', 'Mappings', (mappings, req) => {
			if (!req.user) return req.reject('Session Expired')
			if (mappings.length === 0) {
				return
			}
			const tx = sf.transaction(req)
			return Promise.all(
				mappings
				.filter(mapping => mapping.employeeId)
				.map(mapping => getEmployeeName(mapping, tx))
			)
		})
		//EmployeeName Function
	const getEmployeeName = async(each, tx) => {
			try {
				const empId = each.employeeId
				const data = await tx.get(`/odata/v2/User('${empId}')`)
				each.employeeName = data.defaultFullName
			} catch (e) {
                console.log(e)
				each.employeeName = 'Unknown'
			}
		}
		//Employee Skill set function
	const getSkills = async(each, tx) => {

		try {
			const skillArr = []
			const empId = each.employeeId
			const data = await tx.get(
				`/odata/v2/SkillProfile('${empId}')?$format=json&$expand=externalCodeNav,ratedSkills/skillNav&$select=ratedSkills/skillNav/name_en_US`
			)
			const skills = data.ratedSkills.results
			for (const skill of skills) {
				if (typeof skill != 'object') break
				const skillName = skill.skillNav.name_en_US
				skillArr.push(skillName)
			}
			each.skills = skillArr.toString()
		} catch (e) {
			each.skills = 'No Skills mapped in SuccessFactors'
			return
		}
	}
}