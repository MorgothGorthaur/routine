package com.example.routine.DTO;

import java.sql.Date;
import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.example.routine.Model.DayActuality;
import com.example.routine.validation.CheckIfDateIsActualValidation;
import com.example.routine.validation.CheckIfDateIsUniqueValidation;

/*
 *  Day class without list of events
 */
@CheckIfDateIsUniqueValidation
public class DayDto {
	@NotNull(message = "name must be no null")
	@Size(min=2, max=30, message = "name must have size between 2 and 30 literals")
	private String name;
	@NotNull(message = "date must be seted!")
	@CheckIfDateIsActualValidation

	private Date date;
	private Long id;
	private DayActuality dayActuality;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	public DayActuality getDayActuality() {
		return dayActuality;
	}
	public void setDayActuality(DayActuality dayActuality) {
		this.dayActuality = dayActuality;
	}
	@Override
	public int hashCode() {
		return Objects.hash(date, dayActuality, id, name);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DayDto other = (DayDto) obj;
		return Objects.equals(date, other.date) && dayActuality == other.dayActuality && Objects.equals(id, other.id)
				&& Objects.equals(name, other.name);
	}
	
}
