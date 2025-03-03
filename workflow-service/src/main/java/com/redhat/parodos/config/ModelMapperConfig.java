/*
 * Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.parodos.config;

import com.redhat.parodos.project.dto.ProjectResponseDTO;
import com.redhat.parodos.project.entity.Project;
import com.redhat.parodos.workflow.definition.dto.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.dto.converter.WorkFlowTaskDefinitionDTOConverter;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Model mapper configuration
 *
 * @author Annel Ketcha (Github: anludke)
 * @author Richard Wang (Github: richardw98)
 */
@Configuration
public class ModelMapperConfig {

	@Bean
	ModelMapper modelMapper() {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setAmbiguityIgnored(true);
		addProjectUserMapping(modelMapper);
		addWorkFlowDefinitionResponseDTOMapping(modelMapper);
		return modelMapper;
	}

	private void addWorkFlowDefinitionResponseDTOMapping(ModelMapper modelMapper) {
		PropertyMap<WorkFlowDefinition, WorkFlowDefinitionResponseDTO> workFlowDefinitionResponseDTOMap = new PropertyMap<>() {
			protected void configure() {
				using(new WorkFlowTaskDefinitionDTOConverter()).map(source.getWorkFlowTaskDefinitions()).setWorks(null);
			}
		};
		modelMapper.addMappings(workFlowDefinitionResponseDTOMap);
	}

	private void addProjectUserMapping(ModelMapper modelMapper) {
		PropertyMap<Project, ProjectResponseDTO> projectUserResponseDTOTypeMap = new PropertyMap<Project, ProjectResponseDTO>() {
			protected void configure() {
				map().setUsername(source.getUser().getUsername());
			}
		};
		modelMapper.addMappings(projectUserResponseDTOTypeMap);
	}

}
