using sfextension.refapp as refapp from '../db/data-model';

service catalogService @(requires : 'authenticated-user') {
    entity Project  as projection on refapp.Project;
    entity Mappings as projection on refapp.EmployeeProjectMapping;
    entity Notifications @(restrict : [{
        grant : 'READ',
        where : 'managerId = $user.id'
    }])as projection on refapp.Notifications;

    @cds.persistence.skip
    entity LoggedInUser {
        key id   : String;
        name : String;
    }
}