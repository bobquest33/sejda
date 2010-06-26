/*
 * Created on 29/mag/2010
 * Copyright (C) 2010 by Andrea Vacondio (andrea.vacondio@gmail.com).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.sejda.core.manipulation.model.rotation;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Model for a page rotation
 * 
 * @author Andrea Vacondio
 * 
 */
public class PageRotation implements Serializable {

    private static final long serialVersionUID = 6335354168386687187L;

    private int pageNumber;
    @NotNull
    private Rotation rotation;
    @NotNull
    private RotationType rotationType;

    /**
     * Single page rotation for the given pageNumber and the given {@link Rotation}
     * 
     * @param pageNumber
     * @param rotation
     */
    public PageRotation(int pageNumber, Rotation rotation) {
        this(rotation, RotationType.SINGLE_PAGE);
        this.pageNumber = pageNumber;
    }

    /**
     * Non single page rotation constructor
     * 
     * @param rotation
     * @param rotationType
     */
    public PageRotation(Rotation rotation, RotationType rotationType) {
        this.rotation = rotation;
        this.rotationType = rotationType;
    }

    /**
     * @return the pageNumber
     */
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * @return the rotation
     */
    public Rotation getRotation() {
        return rotation;
    }

    /**
     * @return the rotationType
     */
    public RotationType getRotationType() {
        return rotationType;
    }

    /**
     * @param page
     * @return true if this rotation accept the given page number
     */
    public boolean accept(int page) {
        if (RotationType.ALL_PAGES.equals(rotationType)) {
            return true;
        }
        if (RotationType.ODD_PAGES.equals(rotationType) && (page % 2 != 0)) {
            return true;
        }
        if (RotationType.EVEN_PAGES.equals(rotationType) && (page % 2 == 0)) {
            return true;
        }
        //single page
        return page == pageNumber;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append(pageNumber).append(rotation.getDegrees()).append(rotationType)
                .toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(pageNumber).append(rotation).append(rotationType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PageRotation)) {
            return false;
        }
        PageRotation oRotation = (PageRotation) other;
        return new EqualsBuilder().append(pageNumber, oRotation.getPageNumber()).append(rotation,
                oRotation.getRotation()).append(rotationType, oRotation.getRotationType()).isEquals();
    }

}
