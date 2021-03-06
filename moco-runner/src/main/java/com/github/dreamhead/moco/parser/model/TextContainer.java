package com.github.dreamhead.moco.parser.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.dreamhead.moco.parser.deserializer.TextContainerDeserializer;
import com.github.dreamhead.moco.resource.ContentResource;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.github.dreamhead.moco.Moco.template;
import static com.github.dreamhead.moco.Moco.text;
import static com.github.dreamhead.moco.parser.model.DynamicResponseHandlerFactory.toVariables;
import static com.github.dreamhead.moco.parser.model.Dynamics.invokeTarget;

@JsonDeserialize(using = TextContainerDeserializer.class)
public class TextContainer implements Container {
    private static final String TEMPLATE_NAME = "template";
    private String text;
    private String operation;
    private Map<String, TextContainer> props = ImmutableMap.of();

    private TextContainer(final String text, final String operation,
                            final Map<String, TextContainer> props) {
        this.text = text;
        this.operation = operation;
        this.props = props;
    }

    protected TextContainer() {
    }

    public ContentResource asResource() {
        if (isRawText()) {
            return text(this.text);
        }

        if (isForTemplate()) {
            return asTemplateResource();
        }

        return invokeTarget(getMethodName(), this.text, ContentResource.class);
    }

    public ContentResource asTemplateResource() {
        return asTemplateResource("text");
    }

    private void ensureTemplate() {
        if (!isForTemplate()) {
            throw new IllegalArgumentException(this + " is not a template");
        }
    }

    public ContentResource asTemplateResource(final String resourceName) {
        ensureTemplate();

        if (hasProperties()) {
            return template(invokeTarget(resourceName, this.text, ContentResource.class), toVariables(this.props));
        }

        return template(invokeTarget(resourceName, this.text, ContentResource.class));
    }

    private String getMethodName() {
        if (this.operation.equalsIgnoreCase("path_resource")) {
            return "pathResource";
        }

        return this.operation;
    }

    public boolean isRawText() {
        return this.operation == null && text != null;
    }

    public String getText() {
        return text;
    }

    public String getOperation() {
        return operation;
    }

    public boolean hasProperties() {
        return !this.props.isEmpty();
    }

    public Map<String, TextContainer> getProps() {
        return props;
    }

    public static boolean isForTemplate(final String operation) {
        return TEMPLATE_NAME.equalsIgnoreCase(operation);
    }

    public static String getTemplateName() {
        return TEMPLATE_NAME;
    }

    public boolean isForTemplate() {
        return isForTemplate(this.operation);
    }

    public boolean isFileContainer() {
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("text", text)
                .add("operation", operation)
                .add("properties", props)
                .toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String text;
        private String operation;
        private Map<String, TextContainer> props;

        public Builder withText(final String text) {
            this.text = text;
            return this;
        }

        public Builder withOperation(final String operation) {
            this.operation = operation;
            return this;
        }

        public Builder withProps(final Map<String, TextContainer> props) {
            this.props = props;
            return this;
        }

        public TextContainer build() {
            return new TextContainer(text, operation, asProps(props));
        }

        private Map<String, TextContainer> asProps(final Map<String, TextContainer> props) {
            if (props != null) {
                return props;
            }

            return ImmutableMap.of();
        }
    }
}
