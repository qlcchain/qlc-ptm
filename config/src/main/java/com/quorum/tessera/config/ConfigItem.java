package com.quorum.tessera.config;

import com.quorum.tessera.config.constraints.NoUnmatchedElements;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlAnyElement;
import java.lang.reflect.Field;
import java.util.List;

@NoUnmatchedElements
public abstract class ConfigItem {

    @XmlAnyElement private List<Object> unmatched;

    public List<Object> getUnmatched() {
        return unmatched;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE) {
            @Override
            protected boolean accept(Field f) {
                return super.accept(f) && !f.getName().toLowerCase().contains("password");
            }
        }.toString();
    }
}
