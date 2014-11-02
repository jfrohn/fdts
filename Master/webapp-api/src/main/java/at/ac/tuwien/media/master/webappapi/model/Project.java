package at.ac.tuwien.media.master.webappapi.model;

import java.util.Collection;
import java.util.HashSet;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Project {
    private long m_nId;
    private String m_sName;
    private String m_sDescription;
    private Collection<Group> m_aReadGroups;
    private Collection<Group> m_aReadWriteGroups;

    public Project() {
    }

    public Project(@Nonnegative final long nId, @Nullable final String sName, @Nullable final String sDescription) {
	m_nId = nId;
	m_sName = sName;
	m_sDescription = sDescription;
    }

    public long getId() {
	return m_nId;
    }

    public void setId(@Nonnegative final long nId) {
	m_nId = nId;
    }

    @Nullable
    public String getName() {
	return m_sName;
    }

    public void setName(@Nullable final String sName) {
	m_sName = sName;
    }

    @Nullable
    public String getDescription() {
	return m_sDescription;
    }

    public void setDescription(@Nullable final String sDescription) {
	m_sDescription = sDescription;
    }

    private String _getNames(final Collection<Group> aGroups) {
	final StringBuilder aSB = new StringBuilder();

	boolean bFirst = true;
	for (final Group aGroup : aGroups) {
	    if (bFirst)
		bFirst = false;
	    else
		aSB.append(", ");

	    aSB.append(aGroup.getName());
	}

	return aSB.toString();
    }

    @Nonnull
    public Collection<Group> getReadGroups() {
	if (m_aReadGroups == null)
	    m_aReadGroups = new HashSet<Group>();

	return m_aReadGroups;
    }

    @Nonnull
    public String getReadGroupNames() {
	return _getNames(getReadGroups());
    }

    public void addReadGroup(@Nonnull final Group aReadGroup) {
	getReadGroups().add(aReadGroup);
    }

    @Nonnull
    public Collection<Group> getReadWriteGroups() {
	if (m_aReadWriteGroups == null)
	    m_aReadWriteGroups = new HashSet<Group>();

	return m_aReadWriteGroups;
    }

    @Nonnull
    public String getReadWriteGroupNames() {
	return _getNames(getReadWriteGroups());
    }

    public void addReadWriteGroup(@Nonnull final Group aReadWriteGroup) {
	getReadWriteGroups().add(aReadWriteGroup);
    }
}
