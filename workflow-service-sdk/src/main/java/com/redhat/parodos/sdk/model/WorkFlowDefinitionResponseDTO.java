/*
 * Parodos Workflow Service API
 * This is the API documentation for the Parodos Workflow Service. It provides operations to execute assessments to determine infrastructure options (tooling + environments). Also executes infrastructure task workflows to call downstream systems to stand-up an infrastructure option.
 *
 * The version of the OpenAPI document: v1.0.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package com.redhat.parodos.sdk.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.redhat.parodos.sdk.model.WorkDefinitionResponseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * WorkFlowDefinitionResponseDTO
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen")
public class WorkFlowDefinitionResponseDTO {
  public static final String SERIALIZED_NAME_AUTHOR = "author";
  @SerializedName(SERIALIZED_NAME_AUTHOR)
  private String author;

  public static final String SERIALIZED_NAME_CREATE_DATE = "createDate";
  @SerializedName(SERIALIZED_NAME_CREATE_DATE)
  private Date createDate;

  public static final String SERIALIZED_NAME_ID = "id";
  @SerializedName(SERIALIZED_NAME_ID)
  private UUID id;

  public static final String SERIALIZED_NAME_MODIFY_DATE = "modifyDate";
  @SerializedName(SERIALIZED_NAME_MODIFY_DATE)
  private Date modifyDate;

  public static final String SERIALIZED_NAME_NAME = "name";
  @SerializedName(SERIALIZED_NAME_NAME)
  private String name;

  public static final String SERIALIZED_NAME_PARAMETERS = "parameters";
  @SerializedName(SERIALIZED_NAME_PARAMETERS)
  private Map<String, Map<String, Object>> parameters = null;

  public static final String SERIALIZED_NAME_PROCESSING_TYPE = "processingType";
  @SerializedName(SERIALIZED_NAME_PROCESSING_TYPE)
  private String processingType;

  public static final String SERIALIZED_NAME_TYPE = "type";
  @SerializedName(SERIALIZED_NAME_TYPE)
  private String type;

  public static final String SERIALIZED_NAME_WORKS = "works";
  @SerializedName(SERIALIZED_NAME_WORKS)
  private List<WorkDefinitionResponseDTO> works = null;

  public WorkFlowDefinitionResponseDTO() { 
  }

  public WorkFlowDefinitionResponseDTO author(String author) {
    
    this.author = author;
    return this;
  }

   /**
   * Get author
   * @return author
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public String getAuthor() {
    return author;
  }


  public void setAuthor(String author) {
    this.author = author;
  }


  public WorkFlowDefinitionResponseDTO createDate(Date createDate) {
    
    this.createDate = createDate;
    return this;
  }

   /**
   * Get createDate
   * @return createDate
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public Date getCreateDate() {
    return createDate;
  }


  public void setCreateDate(Date createDate) {
    this.createDate = createDate;
  }


  public WorkFlowDefinitionResponseDTO id(UUID id) {
    
    this.id = id;
    return this;
  }

   /**
   * Get id
   * @return id
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public UUID getId() {
    return id;
  }


  public void setId(UUID id) {
    this.id = id;
  }


  public WorkFlowDefinitionResponseDTO modifyDate(Date modifyDate) {
    
    this.modifyDate = modifyDate;
    return this;
  }

   /**
   * Get modifyDate
   * @return modifyDate
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public Date getModifyDate() {
    return modifyDate;
  }


  public void setModifyDate(Date modifyDate) {
    this.modifyDate = modifyDate;
  }


  public WorkFlowDefinitionResponseDTO name(String name) {
    
    this.name = name;
    return this;
  }

   /**
   * Get name
   * @return name
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public String getName() {
    return name;
  }


  public void setName(String name) {
    this.name = name;
  }


  public WorkFlowDefinitionResponseDTO parameters(Map<String, Map<String, Object>> parameters) {
    
    this.parameters = parameters;
    return this;
  }

  public WorkFlowDefinitionResponseDTO putParametersItem(String key, Map<String, Object> parametersItem) {
    if (this.parameters == null) {
      this.parameters = new HashMap<String, Map<String, Object>>();
    }
    this.parameters.put(key, parametersItem);
    return this;
  }

   /**
   * Get parameters
   * @return parameters
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public Map<String, Map<String, Object>> getParameters() {
    return parameters;
  }


  public void setParameters(Map<String, Map<String, Object>> parameters) {
    this.parameters = parameters;
  }


  public WorkFlowDefinitionResponseDTO processingType(String processingType) {
    
    this.processingType = processingType;
    return this;
  }

   /**
   * Get processingType
   * @return processingType
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public String getProcessingType() {
    return processingType;
  }


  public void setProcessingType(String processingType) {
    this.processingType = processingType;
  }


  public WorkFlowDefinitionResponseDTO type(String type) {
    
    this.type = type;
    return this;
  }

   /**
   * Get type
   * @return type
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public String getType() {
    return type;
  }


  public void setType(String type) {
    this.type = type;
  }


  public WorkFlowDefinitionResponseDTO works(List<WorkDefinitionResponseDTO> works) {
    
    this.works = works;
    return this;
  }

  public WorkFlowDefinitionResponseDTO addWorksItem(WorkDefinitionResponseDTO worksItem) {
    if (this.works == null) {
      this.works = new ArrayList<WorkDefinitionResponseDTO>();
    }
    this.works.add(worksItem);
    return this;
  }

   /**
   * Get works
   * @return works
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public List<WorkDefinitionResponseDTO> getWorks() {
    return works;
  }


  public void setWorks(List<WorkDefinitionResponseDTO> works) {
    this.works = works;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WorkFlowDefinitionResponseDTO workFlowDefinitionResponseDTO = (WorkFlowDefinitionResponseDTO) o;
    return Objects.equals(this.author, workFlowDefinitionResponseDTO.author) &&
        Objects.equals(this.createDate, workFlowDefinitionResponseDTO.createDate) &&
        Objects.equals(this.id, workFlowDefinitionResponseDTO.id) &&
        Objects.equals(this.modifyDate, workFlowDefinitionResponseDTO.modifyDate) &&
        Objects.equals(this.name, workFlowDefinitionResponseDTO.name) &&
        Objects.equals(this.parameters, workFlowDefinitionResponseDTO.parameters) &&
        Objects.equals(this.processingType, workFlowDefinitionResponseDTO.processingType) &&
        Objects.equals(this.type, workFlowDefinitionResponseDTO.type) &&
        Objects.equals(this.works, workFlowDefinitionResponseDTO.works);
  }

  @Override
  public int hashCode() {
    return Objects.hash(author, createDate, id, modifyDate, name, parameters, processingType, type, works);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WorkFlowDefinitionResponseDTO {\n");
    sb.append("    author: ").append(toIndentedString(author)).append("\n");
    sb.append("    createDate: ").append(toIndentedString(createDate)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    modifyDate: ").append(toIndentedString(modifyDate)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    parameters: ").append(toIndentedString(parameters)).append("\n");
    sb.append("    processingType: ").append(toIndentedString(processingType)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    works: ").append(toIndentedString(works)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

