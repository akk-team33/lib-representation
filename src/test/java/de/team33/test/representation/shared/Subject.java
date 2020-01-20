package de.team33.test.representation.shared;

import java.util.*;

import static java.util.Collections.*;

public class Subject {

    private int thePrimitive;
    private String theString;
    private Number theNumber;
    private byte[] theByteArray = {};
    private Object theObject;
    private List<Subject> theSubjectList;
    private Set<Subject> theSubjectSet;

    public final int getThePrimitive() {
        return thePrimitive;
    }

    public final Subject setThePrimitive(final int thePrimitive) {
        this.thePrimitive = thePrimitive;
        return this;
    }

    public final String getTheString() {
        return theString;
    }

    public final Subject setTheString(final String theString) {
        this.theString = theString;
        return this;
    }

    public final Number getTheNumber() {
        return theNumber;
    }

    public final Subject setTheNumber(final Number theNumber) {
        this.theNumber = theNumber;
        return this;
    }

    public final byte[] getTheByteArray() {
        return theByteArray.clone();
    }

    public final Subject setTheByteArray(final byte[] theByteArray) {
        this.theByteArray = theByteArray.clone();
        return this;
    }

    public final Object getTheObject() {
        return theObject;
    }

    public final Subject setTheObject(final Object theObject) {
        this.theObject = theObject;
        return this;
    }

    public final List<Subject> getTheSubjectList() {
        return (null == theSubjectList) ? null : unmodifiableList(theSubjectList);
    }

    public final Subject setTheSubjectList(final Collection<? extends Subject> theSubjectList) {
        this.theSubjectList = (null == theSubjectList) ? null : new ArrayList<>(theSubjectList);
        return this;
    }

    public final Set<Subject> getTheSubjectSet() {
        return (null == theSubjectSet) ? null : unmodifiableSet(theSubjectSet);
    }

    public final Subject setTheSubjectSet(final Collection<? extends Subject> theSubjectSet) {
        this.theSubjectSet = (null == theSubjectSet) ? null : new HashSet<>(theSubjectSet);
        return this;
    }
}
