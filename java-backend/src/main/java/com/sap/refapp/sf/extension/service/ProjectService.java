package com.sap.refapp.sf.extension.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.refapp.sf.extension.model.Project;
import com.sap.refapp.sf.extension.repository.ProjectRepository;

@Repository
@Transactional
public class ProjectService {

	private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

	@Autowired
	private ProjectRepository projectRepository;

	public Iterable<Project> getAllProjects() throws DataAccessException {

		Iterable<Project> projects = projectRepository.findAll();
		return projects;
	}

	public Project getProjectById(int projId) {
		return projectRepository.findProjectById(projId);
	}

	public Project saveProject(Project project) {
		return projectRepository.save(project);
	}

	public void saveProject(List<Project> projects) {
		projectRepository.saveAll(projects);
	}

	public void loadProject(String location) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		TypeReference<List<Project>> typeReference = new TypeReference<List<Project>>() {
		};
		InputStream inputStream = null;
		try {
			inputStream = TypeReference.class.getResourceAsStream(location);
			List<Project> listOfProducts = mapper.readValue(inputStream, typeReference);
			logger.info("proj no:" + listOfProducts.size());
			saveProject(listOfProducts);
		} catch (IOException e) {
			throw e;
		}
	}

	public void loadProjectFromJSON(String json) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		TypeReference<Project> typeReference = new TypeReference<Project>() {
		};
		
		try {
			Project project = mapper.readValue(json, typeReference);
			logger.info("project saved with id: " + project.getProjectId());
			saveProject(project);
		} catch (IOException e) {
			throw e;
		}
	}

}