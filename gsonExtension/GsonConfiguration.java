package com.google.gson.gsonExtension;



import com.google.gson.*;
import com.google.gson.internal.Excluder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GsonConfiguration {
    final Excluder excluder;
    final FieldNamingStrategy fieldNamingStrategy;
    public final Map<Type, InstanceCreator<?>> instanceCreators;
    final boolean serializeNulls;
    final boolean complexMapKeySerialization;
    final boolean generateNonExecutableJson;
    final boolean htmlSafe;
    final FormattingStyle formattingStyle;
    final Strictness strictness;
    final boolean serializeSpecialFloatingPointValues;
    public final boolean useJdkUnsafe;
    final String datePattern;
    final int dateStyle;
    final int timeStyle;
    final LongSerializationPolicy longSerializationPolicy;
    public final List<TypeAdapterFactory> builderFactories;
    final List<TypeAdapterFactory> builderHierarchyFactories;
    final ToNumberStrategy objectToNumberStrategy;
    final ToNumberStrategy numberToNumberStrategy;
    final List<ReflectionAccessFilter> reflectionFilters;

    private GsonConfiguration(Builder builder) {
        this.excluder = builder.excluder;
        this.fieldNamingStrategy = builder.fieldNamingStrategy;
        this.instanceCreators = builder.instanceCreators != null ?
                Collections.unmodifiableMap(new HashMap<>(builder.instanceCreators)) :
                Collections.emptyMap();
        this.serializeNulls = builder.serializeNulls;
        this.complexMapKeySerialization = builder.complexMapKeySerialization;
        this.generateNonExecutableJson = builder.generateNonExecutableJson;
        this.htmlSafe = builder.htmlSafe;
        this.formattingStyle = builder.formattingStyle;
        this.strictness = builder.strictness;
        this.serializeSpecialFloatingPointValues = builder.serializeSpecialFloatingPointValues;
        this.useJdkUnsafe = builder.useJdkUnsafe;
        this.datePattern = builder.datePattern;
        this.dateStyle = builder.dateStyle;
        this.timeStyle = builder.timeStyle;
        this.longSerializationPolicy = builder.longSerializationPolicy != null ?
                builder.longSerializationPolicy :
                LongSerializationPolicy.DEFAULT;
        this.builderFactories = builder.builderFactories != null ?
                Collections.unmodifiableList(new ArrayList<>(builder.builderFactories)) :
                Collections.emptyList();
        this.builderHierarchyFactories = builder.builderHierarchyFactories != null ?
                Collections.unmodifiableList(new ArrayList<>(builder.builderHierarchyFactories)) :
                Collections.emptyList();
        this.objectToNumberStrategy = builder.objectToNumberStrategy;
        this.numberToNumberStrategy = builder.numberToNumberStrategy;
        this.reflectionFilters = builder.reflectionFilters != null ?
                Collections.unmodifiableList(new ArrayList<>(builder.reflectionFilters)) :
                Collections.emptyList();
    }

    public static class Builder {
        private Excluder excluder = Excluder.DEFAULT;
        private FieldNamingStrategy fieldNamingStrategy = Gson.DEFAULT_FIELD_NAMING_STRATEGY;
        private Map<Type, InstanceCreator<?>> instanceCreators;
        private boolean serializeNulls = Gson.DEFAULT_SERIALIZE_NULLS;
        private boolean complexMapKeySerialization = Gson.DEFAULT_COMPLEX_MAP_KEYS;
        private boolean generateNonExecutableJson = Gson.DEFAULT_JSON_NON_EXECUTABLE;
        private boolean htmlSafe = Gson.DEFAULT_ESCAPE_HTML;
        private FormattingStyle formattingStyle = Gson.DEFAULT_FORMATTING_STYLE;
        private Strictness strictness = Gson.DEFAULT_STRICTNESS;
        private boolean serializeSpecialFloatingPointValues = Gson.DEFAULT_SPECIALIZE_FLOAT_VALUES;
        private boolean useJdkUnsafe = Gson.DEFAULT_USE_JDK_UNSAFE;
        private String datePattern = Gson.DEFAULT_DATE_PATTERN;
        private int dateStyle;
        private int timeStyle;
        private LongSerializationPolicy longSerializationPolicy;
        private List<TypeAdapterFactory> builderFactories;
        private List<TypeAdapterFactory> builderHierarchyFactories;
        private ToNumberStrategy objectToNumberStrategy = Gson.DEFAULT_OBJECT_TO_NUMBER_STRATEGY;
        private ToNumberStrategy numberToNumberStrategy = Gson.DEFAULT_NUMBER_TO_NUMBER_STRATEGY;
        private List<ReflectionAccessFilter> reflectionFilters;

        public Builder() {
            // Default constructor
        }

        public Builder(GsonConfiguration configuration) {
            this.excluder = configuration.excluder;
            this.fieldNamingStrategy = configuration.fieldNamingStrategy;
            this.instanceCreators = new HashMap<>(configuration.instanceCreators);
            this.serializeNulls = configuration.serializeNulls;
            this.complexMapKeySerialization = configuration.complexMapKeySerialization;
            this.generateNonExecutableJson = configuration.generateNonExecutableJson;
            this.htmlSafe = configuration.htmlSafe;
            this.formattingStyle = configuration.formattingStyle;
            this.strictness = configuration.strictness;
            this.serializeSpecialFloatingPointValues = configuration.serializeSpecialFloatingPointValues;
            this.useJdkUnsafe = configuration.useJdkUnsafe;
            this.datePattern = configuration.datePattern;
            this.dateStyle = configuration.dateStyle;
            this.timeStyle = configuration.timeStyle;
            this.longSerializationPolicy = configuration.longSerializationPolicy;
            this.builderFactories = new ArrayList<>(configuration.builderFactories);
            this.builderHierarchyFactories = new ArrayList<>(configuration.builderHierarchyFactories);
            this.objectToNumberStrategy = configuration.objectToNumberStrategy;
            this.numberToNumberStrategy = configuration.numberToNumberStrategy;
            this.reflectionFilters = new ArrayList<>(configuration.reflectionFilters);
        }

        public Builder setExcluder(Excluder excluder) {
            this.excluder = excluder;
            return this;
        }

        public Builder setFieldNamingStrategy(FieldNamingStrategy fieldNamingStrategy) {
            this.fieldNamingStrategy = fieldNamingStrategy;
            return this;
        }

        public Builder setInstanceCreator(Type type, InstanceCreator<?> instanceCreator) {
            if (instanceCreators == null) {
                instanceCreators = new HashMap<>();
            }
            instanceCreators.put(type, instanceCreator);
            return this;
        }

        public Builder setSerializeNulls(boolean serializeNulls) {
            this.serializeNulls = serializeNulls;
            return this;
        }

        public Builder setComplexMapKeySerialization(boolean complexMapKeySerialization) {
            this.complexMapKeySerialization = complexMapKeySerialization;
            return this;
        }

        public Builder setGenerateNonExecutableJson(boolean generateNonExecutableJson) {
            this.generateNonExecutableJson = generateNonExecutableJson;
            return this;
        }

        public Builder setHtmlSafe(boolean htmlSafe) {
            this.htmlSafe = htmlSafe;
            return this;
        }

        public Builder setFormattingStyle(FormattingStyle formattingStyle) {
            this.formattingStyle = formattingStyle;
            return this;
        }

        public Builder setStrictness(Strictness strictness) {
            this.strictness = strictness;
            return this;
        }

        public Builder setSerializeSpecialFloatingPointValues(boolean serializeSpecialFloatingPointValues) {
            this.serializeSpecialFloatingPointValues = serializeSpecialFloatingPointValues;
            return this;
        }

        public Builder setUseJdkUnsafe(boolean useJdkUnsafe) {
            this.useJdkUnsafe = useJdkUnsafe;
            return this;
        }

        public Builder setDatePattern(String datePattern) {
            this.datePattern = datePattern;
            return this;
        }

        public Builder setDateStyle(int dateStyle) {
            this.dateStyle = dateStyle;
            return this;
        }

        public Builder setTimeStyle(int timeStyle) {
            this.timeStyle = timeStyle;
            return this;
        }

        public Builder setLongSerializationPolicy(LongSerializationPolicy longSerializationPolicy) {
            this.longSerializationPolicy = longSerializationPolicy;
            return this;
        }

        public Builder addBuilderFactory(TypeAdapterFactory factory) {
            if (builderFactories == null) {
                builderFactories = new ArrayList<>();
            }
            builderFactories.add(factory);
            return this;
        }

        public Builder addBuilderHierarchyFactory(TypeAdapterFactory factory) {
            if (builderHierarchyFactories == null) {
                builderHierarchyFactories = new ArrayList<>();
            }
            builderHierarchyFactories.add(factory);
            return this;
        }

        public Builder setObjectToNumberStrategy(ToNumberStrategy objectToNumberStrategy) {
            this.objectToNumberStrategy = objectToNumberStrategy;
            return this;
        }

        public Builder setNumberToNumberStrategy(ToNumberStrategy numberToNumberStrategy) {
            this.numberToNumberStrategy = numberToNumberStrategy;
            return this;
        }

        public Builder addReflectionFilter(ReflectionAccessFilter filter) {
            if (reflectionFilters == null) {
                reflectionFilters = new ArrayList<>();
            }
            reflectionFilters.add(filter);
            return this;
        }

        public Builder setReflectionFilters(List<ReflectionAccessFilter> reflectionFilters) {
            this.reflectionFilters = new ArrayList<>(reflectionFilters);
            return this;
        }

        public Builder setBuilderFactories(List<TypeAdapterFactory> factories) {
            this.builderFactories = new ArrayList<>(factories);
            return this;
        }

        public Builder setBuilderHierarchyFactories(List<TypeAdapterFactory> factories) {
            this.builderHierarchyFactories = new ArrayList<>(factories);
            return this;
        }

        public GsonConfiguration build() {
            return new GsonConfiguration(this);
        }

        // Getters for all fields to allow inspection of current state
        public Excluder getExcluder() {
            return excluder;
        }

        public FieldNamingStrategy getFieldNamingStrategy() {
            return fieldNamingStrategy;
        }

        public Map<Type, InstanceCreator<?>> getInstanceCreators() {
            return instanceCreators != null ? Collections.unmodifiableMap(instanceCreators) : Collections.emptyMap();
        }

        public boolean isSerializeNulls() {
            return serializeNulls;
        }

        public boolean isComplexMapKeySerialization() {
            return complexMapKeySerialization;
        }

        public boolean isGenerateNonExecutableJson() {
            return generateNonExecutableJson;
        }

        public boolean isHtmlSafe() {
            return htmlSafe;
        }

        public FormattingStyle getFormattingStyle() {
            return formattingStyle;
        }

        public Strictness getStrictness() {
            return strictness;
        }

        public boolean isSerializeSpecialFloatingPointValues() {
            return serializeSpecialFloatingPointValues;
        }

        public boolean isUseJdkUnsafe() {
            return useJdkUnsafe;
        }

        public String getDatePattern() {
            return datePattern;
        }

        public int getDateStyle() {
            return dateStyle;
        }

        public int getTimeStyle() {
            return timeStyle;
        }

        public LongSerializationPolicy getLongSerializationPolicy() {
            return longSerializationPolicy;
        }

        public List<TypeAdapterFactory> getBuilderFactories() {
            return builderFactories != null ? Collections.unmodifiableList(builderFactories) : Collections.emptyList();
        }

        public List<TypeAdapterFactory> getBuilderHierarchyFactories() {
            return builderHierarchyFactories != null ? Collections.unmodifiableList(builderHierarchyFactories) : Collections.emptyList();
        }

        public ToNumberStrategy getObjectToNumberStrategy() {
            return objectToNumberStrategy;
        }

        public ToNumberStrategy getNumberToNumberStrategy() {
            return numberToNumberStrategy;
        }

        public List<ReflectionAccessFilter> getReflectionFilters() {
            return reflectionFilters != null ? Collections.unmodifiableList(reflectionFilters) : Collections.emptyList();
        }
    }

    // Getters for all fields to allow inspection of current state
    public Excluder getExcluder() {
        return excluder;
    }

    public FieldNamingStrategy getFieldNamingStrategy() {
        return fieldNamingStrategy;
    }

    public Map<Type, InstanceCreator<?>> getInstanceCreators() {
        return instanceCreators;
    }

    public boolean isSerializeNulls() {
        return serializeNulls;
    }

    public boolean isComplexMapKeySerialization() {
        return complexMapKeySerialization;
    }

    public boolean isGenerateNonExecutableJson() {
        return generateNonExecutableJson;
    }

    public boolean isHtmlSafe() {
        return htmlSafe;
    }

    public FormattingStyle getFormattingStyle() {
        return formattingStyle;
    }

    public Strictness getStrictness() {
        return strictness;
    }

    public boolean isSerializeSpecialFloatingPointValues() {
        return serializeSpecialFloatingPointValues;
    }

    public boolean isUseJdkUnsafe() {
        return useJdkUnsafe;
    }

    public String getDatePattern() {
        return datePattern;
    }

    public int getDateStyle() {
        return dateStyle;
    }

    public int getTimeStyle() {
        return timeStyle;
    }

    public LongSerializationPolicy getLongSerializationPolicy() {
        return longSerializationPolicy;
    }

    public List<TypeAdapterFactory> getBuilderFactories() {
        return builderFactories;
    }

    public List<TypeAdapterFactory> getBuilderHierarchyFactories() {
        return builderHierarchyFactories;
    }

    public ToNumberStrategy getObjectToNumberStrategy() {
        return objectToNumberStrategy;
    }

    public ToNumberStrategy getNumberToNumberStrategy() {
        return numberToNumberStrategy;
    }

    public List<ReflectionAccessFilter> getReflectionFilters() {
        return reflectionFilters;
    }
}