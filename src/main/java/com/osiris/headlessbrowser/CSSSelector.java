package com.osiris.headlessbrowser;

/**
 /**
 * Example usage of the CSSSelector class:
 * <pre>
 *     public static void main(String[] args) {
 *     String selector = new CSSSelector().element("div")
 *             .clazz("container")
 *             .id("main")
 *             .pseudoClass("hover")
 *             .attribute("data-type", "=", "example")
 *             .child()
 *             .element("span")
 *             .generalSibling()
 *             .element("p")
 *             .toString();
 *
 *     System.out.println(selector); // Or use in HBrowser methods
 * }
 * </pre>
 */
public class CSSSelector {

    private StringBuilder selector;

    /**
     * Constructor to initialize the CSSSelector object.
     * This object helps in programmatically building CSS selector strings.
     */
    public CSSSelector() {
        this.selector = new StringBuilder();
    }

    /**
     * Adds an ID selector to the current selector string.
     *
     * @param id the ID value to add.
     * @return the updated CSSSelector object.
     */
    public CSSSelector id(String id) {
        selector.append("#").append(id);
        return this;
    }

    /**
     * Adds a class selector to the current selector string.
     *
     * @param className the class name to add.
     * @return the updated CSSSelector object.
     */
    public CSSSelector clazz(String className) {
        selector.append(".").append(className);
        return this;
    }

    /**
     * Adds an element selector to the current selector string.
     *
     * @param element the element name to add.
     * @return the updated CSSSelector object.
     */
    public CSSSelector element(String element) {
        selector.append(element);
        return this;
    }

    /**
     * Adds a pseudo-class to the current selector string.
     *
     * @param pseudo the pseudo-class to add.
     * @return the updated CSSSelector object.
     */
    public CSSSelector pseudoClass(String pseudo) {
        selector.append(":").append(pseudo);
        return this;
    }

    /**
     * Adds a pseudo-element to the current selector string.
     *
     * @param pseudoElement the pseudo-element to add.
     * @return the updated CSSSelector object.
     */
    public CSSSelector pseudoElement(String pseudoElement) {
        selector.append("::").append(pseudoElement);
        return this;
    }

    /**
     * Adds an attribute selector to the current selector string.
     *
     * @param attribute the attribute name.
     * @param operator the operator for comparison (e.g., =, ~=, ^=, etc.).
     * @param value the value of the attribute.
     * @return the updated CSSSelector object.
     */
    public CSSSelector attribute(String attribute, String operator, String value) {
        selector.append("[").append(attribute);
        if (operator != null && !operator.isEmpty()) {
            selector.append(operator).append("\"").append(value).append("\"");
        }
        selector.append("]");
        return this;
    }

    /**
     * Adds a combinator (e.g., >, +, ~, or a space) to the current selector string.
     *
     * @param combinator the combinator to add.
     * @return the updated CSSSelector object.
     */
    public CSSSelector combinator(String combinator) {
        selector.append(" ").append(combinator).append(" ");
        return this;
    }

    /**
     * Adds a universal selector (*) to the current selector string.
     *
     * @return the updated CSSSelector object.
     */
    public CSSSelector universal() {
        selector.append("*");
        return this;
    }

    /**
     * Adds a child selector (>) to the current selector string.
     *
     * @return the updated CSSSelector object.
     */
    public CSSSelector child() {
        selector.append(" > ");
        return this;
    }

    /**
     * Adds an adjacent sibling combinator (+) to the current selector string.
     *
     * @return the updated CSSSelector object.
     */
    public CSSSelector adjacentSibling() {
        selector.append(" + ");
        return this;
    }

    /**
     * Adds a general sibling combinator (~) to the current selector string.
     *
     * @return the updated CSSSelector object.
     */
    public CSSSelector generalSibling() {
        selector.append(" ~ ");
        return this;
    }

    /**
     * Clears the current selector string.
     *
     * @return the updated CSSSelector object.
     */
    public CSSSelector clear() {
        selector.setLength(0);
        return this;
    }

    /**
     * Converts the current selector to a string representation.
     *
     * @return the CSS selector string.
     */
    @Override
    public String toString() {
        return selector.toString().trim();
    }
}

