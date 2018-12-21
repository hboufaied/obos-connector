package com.mstools.connector.obos.exception;

import javax.validation.constraints.NotNull;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ObosConnectorException extends Exception{

	private static final long serialVersionUID = -1607147166370813377L;

	@JsonProperty("code")
    private Integer code = null;

    @JsonProperty("message")
    private String message = null;

    @JsonProperty("description")
    private String description = null;
    
    
    /**
	 * @param code
	 * @param message
	 * @param description
	 */
	public ObosConnectorException(Integer code, String message, String description) {
		super();
		this.code = code;
		this.message = message;
		this.description = description;
	}

	/**
	 * @param code
	 * @param message
	 */
	public ObosConnectorException(Integer code, String message) {
		super();
		this.code = code;
		this.message = message;
	}


	/**
     * Get code
     *
     * @return code
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    /**
     * Get message
     *
     * @return message
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get fields
     *
     * @return fields
     **/


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"code\": \"").append(toIndentedString(code)).append("\",");
        sb.append("\"message\": \"").append(toIndentedString(message)).append("\",");
        sb.append("\"description\": \"").append(toIndentedString(description)).append("\"}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
