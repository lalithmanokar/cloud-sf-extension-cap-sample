package com.sap.refapp.sf.extension.repository;

import com.sap.refapp.sf.extension.model.Project;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import org.springframework.stereotype.Repository;

/**
 * This is the project repository interface 
 * which is responsible for communicating with database.
 *
 */
@Repository
public interface ProjectRepository extends CrudRepository<Project, String>{

	@Query(value = "SELECT * FROM PROJECT WHERE PROJECT_ID = ?1", nativeQuery = true)
	Project findProjectById(int projId);

}