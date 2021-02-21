package io.ebeaninternal.server.type;

import io.ebean.annotation.DbArray;
import io.ebean.annotation.DbEnumType;
import io.ebean.annotation.DbEnumValue;
import io.ebean.annotation.EnumValue;
import io.ebean.config.JsonConfig;
import io.ebean.annotation.Platform;
import io.ebean.config.ScalarTypeConverter;
import io.ebean.config.ServerConfig;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.util.AnnotationUtil;
import io.ebeaninternal.dbmigration.DbOffline;
import io.ebeaninternal.api.ExtraTypeFactory;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeanservice.docstore.api.mapping.DocPropertyType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.AttributeConverter;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.Month;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Currency;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of TypeManager.
 * <p>
 * Manages the list of ScalarType that is available.
 * </p>
 */
public final class DefaultTypeManager implements TypeManager {

  private static final Logger logger = LoggerFactory.getLogger(DefaultTypeManager.class);

  private final ConcurrentHashMap<Class<?>, ScalarType<?>> typeMap;

  private final ConcurrentHashMap<Integer, ScalarType<?>> nativeMap;

  private final DefaultTypeFactory extraTypeFactory;

  private final ScalarType<?> hstoreType = new ScalarTypePostgresHstore();

  private final ScalarTypeFile fileType = new ScalarTypeFile();

  private final ScalarType<?> charType = new ScalarTypeChar();

  private final ScalarType<?> charArrayType = new ScalarTypeCharArray();

  private final ScalarType<?> longVarcharType = new ScalarTypeLongVarchar();

  private final ScalarType<?> clobType = new ScalarTypeClob();

  private final ScalarType<?> byteType = new ScalarTypeByte();

  private final ScalarType<?> binaryType = new ScalarTypeBytesBinary();

  private final ScalarType<?> blobType = new ScalarTypeBytesBlob();

  private final ScalarType<?> varbinaryType = new ScalarTypeBytesVarbinary();

  private final ScalarType<?> longVarbinaryType = new ScalarTypeBytesLongVarbinary();

  private final ScalarType<?> shortType = new ScalarTypeShort();

  private final ScalarType<?> integerType = new ScalarTypeInteger();

  private final ScalarType<?> longType = new ScalarTypeLong();

  private final ScalarType<?> doubleType = new ScalarTypeDouble();

  private final ScalarType<?> floatType = new ScalarTypeFloat();

  private final ScalarType<?> bigDecimalType = new ScalarTypeBigDecimal();

  private final ScalarType<?> timeType = new ScalarTypeTime();

  private final ScalarType<?> dateType = new ScalarTypeDate();

  private final ScalarType<?> inetAddressType = new ScalarTypeInetAddress();
  private final ScalarType<?> urlType = new ScalarTypeURL();
  private final ScalarType<?> uriType = new ScalarTypeURI();
  private final ScalarType<?> localeType = new ScalarTypeLocale();
  private final ScalarType<?> currencyType = new ScalarTypeCurrency();
  private final ScalarType<?> timeZoneType = new ScalarTypeTimeZone();

  private final ScalarType<?> stringType = new ScalarTypeString();

  private final ScalarType<?> classType = new ScalarTypeClass();

  private final JsonConfig.DateTime jsonDateTime;

  private final Object objectMapper;

  private final boolean java7Present;

  private final boolean postgres;

  private final boolean offlineMigrationGeneration;

  // OPTIONAL ScalarTypes registered if Jackson/JsonNode is in the classpath

  /**
   * Jackson's JsonNode storage to Clob.
   */
  private ScalarType<?> jsonNodeClob;
  /**
   * Jackson's JsonNode storage to Blob.
   */
  private ScalarType<?> jsonNodeBlob;
  /**
   * Jackson's JsonNode storage to Varchar.
   */
  private ScalarType<?> jsonNodeVarchar;
  /**
   * Jackson's JsonNode storage to Postgres JSON or Clob.
   */
  private ScalarType<?> jsonNodeJson;
  /**
   * Jackson's JsonNode storage to Postgres JSONB or Clob.
   */
  private ScalarType<?> jsonNodeJsonb;

  private final PlatformArrayTypeFactory arrayTypeListFactory;
  private final PlatformArrayTypeFactory arrayTypeSetFactory;

  /**
   * Create the DefaultTypeManager.
   */
  public DefaultTypeManager(ServerConfig config, BootupClasses bootupClasses) {

    this.java7Present = config.getClassLoadConfig().isJava7Present();
    this.jsonDateTime = config.getJsonDateTime();
    this.typeMap = new ConcurrentHashMap<>();
    this.nativeMap = new ConcurrentHashMap<>();

    boolean objectMapperPresent = config.getClassLoadConfig().isJacksonObjectMapperPresent();
    this.objectMapper = (objectMapperPresent) ? initObjectMapper(config) : null;

    this.extraTypeFactory = new DefaultTypeFactory(config);
    this.postgres = isPostgres(config.getDatabasePlatform());
    this.arrayTypeListFactory = arrayTypeListFactory(postgres, config.getDatabasePlatform());
    this.arrayTypeSetFactory = arrayTypeSetFactory(postgres, config.getDatabasePlatform());

    this.offlineMigrationGeneration = DbOffline.isGenerateMigration();

    initialiseStandard(jsonDateTime, config);
    initialiseJavaTimeTypes(jsonDateTime, config);
    initialiseJodaTypes(jsonDateTime, config);
    initialiseJacksonTypes(config);

    loadTypesFromProviders(config, objectMapper);

    if (bootupClasses != null) {
      initialiseCustomScalarTypes(bootupClasses);
      initialiseScalarConverters(bootupClasses);
      initialiseAttributeConverters(bootupClasses);
    }
  }

  /**
   * Return the factory to use to support DB ARRAY types.
   */
  private PlatformArrayTypeFactory arrayTypeListFactory(boolean postgres, DatabasePlatform databasePlatform) {
    if (postgres) {
      return ScalarTypeArrayList.factory();
    } else if (databasePlatform.isPlatform(Platform.H2)) {
      return ScalarTypeArrayListH2.factory();
    }
    // not supported for this DB platform
    return null;
  }

  /**
   * Return the factory to use to support DB ARRAY types.
   */
  private PlatformArrayTypeFactory arrayTypeSetFactory(boolean postgres, DatabasePlatform databasePlatform) {
    if (postgres) {
      return ScalarTypeArraySet.factory();
    } else if (databasePlatform.isPlatform(Platform.H2)) {
      return ScalarTypeArraySetH2.factory();
    }
    // not supported for this DB platform
    return null;
  }

  /**
   * Load custom scalar types registered via ExtraTypeFactory and ServiceLoader.
   */
  private void loadTypesFromProviders(ServerConfig config, Object objectMapper) {

    ServiceLoader<ExtraTypeFactory> factories = ServiceLoader.load(ExtraTypeFactory.class);
    Iterator<ExtraTypeFactory> iterator = factories.iterator();
    if (iterator.hasNext()) {
      // use the cacheFactory (via classpath service loader)
      ExtraTypeFactory plugin = iterator.next();
      List<? extends ScalarType<?>> types = plugin.createTypes(config, objectMapper);
      for (ScalarType<?> type : types) {
        logger.debug("adding ScalarType {}", type.getClass());
        addCustomType(type);
      }
    }
  }

  private boolean isPostgres(DatabasePlatform databasePlatform) {
    return databasePlatform.getPlatform() == Platform.POSTGRES;
  }

  /**
   * Register a custom ScalarType.
   */
  @Override
  public void add(ScalarType<?> scalarType) {
    typeMap.put(scalarType.getType(), scalarType);
    logAdd(scalarType);
  }

  /**
   * Register the ScalarType for an enum. This is special in the sense that an Enum
   * can have many classes if it uses method overrides and we need to register all
   * the variations/classes for the enum.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public void addEnumType(ScalarType<?> scalarType, Class<? extends Enum> enumClass) {

    Set<Class<?>> mappedClasses = new HashSet<>();
    for (Object value : EnumSet.allOf(enumClass).toArray()) {
      mappedClasses.add(value.getClass());
    }
    for (Class<?> cls : mappedClasses) {
      typeMap.put(cls, scalarType);
    }
    logAdd(scalarType);
  }

  private void logAdd(ScalarType<?> scalarType) {
    if (logger.isTraceEnabled()) {
      String msg = "ScalarType register [" + scalarType.getClass().getName() + "]";
      msg += " for [" + scalarType.getType().getName() + "]";
      logger.trace(msg);
    }
  }

  /**
   * Return the ScalarType for the given jdbc type as per java.sql.Types.
   */
  @Override
  public ScalarType<?> getScalarType(int jdbcType) {
    return nativeMap.get(jdbcType);
  }

  /**
   * This can return null if no matching ScalarType is found.
   */
  @Override
  public ScalarType<?> getScalarType(Class<?> type) {
    ScalarType<?> found = typeMap.get(type);
    if (found == null) {
      if (type.getName().equals("org.joda.time.LocalTime")) {
        throw new IllegalStateException(
          "ScalarType of Joda LocalTime not defined. You need to set ServerConfig.jodaLocalTimeMode to"
            + " either 'normal' or 'utc'.  UTC is the old mode using UTC timezone but local time zone is now preferred as 'normal' mode.");
      }
      found = checkInterfaceTypes(type);
    }
    return found;
  }

  private ScalarType<?> checkInterfaceTypes(Class<?> type) {
    if (java7Present) {
      return checkJava7InterfaceTypes(type);
    }
    return null;
  }

  private ScalarType<?> checkJava7InterfaceTypes(Class<?> type) {
    if (java.nio.file.Path.class.isAssignableFrom(type)) {
      return typeMap.get(java.nio.file.Path.class);
    }
    return null;
  }

  @Override
  public ScalarType<?> getHstoreScalarType() {
    return (postgres) ? hstoreType : ScalarTypeJsonMap.typeFor(false, Types.VARCHAR);
  }

  @Override
  public ScalarType<?> getArrayScalarType(Class<?> type, DbArray dbArray, Type genericType) {

    Type valueType = getValueType(genericType);
    if (type.equals(List.class)) {
      if (arrayTypeListFactory != null) {
        return arrayTypeListFactory.typeFor(valueType);
      }
      // fallback to JSON storage in VARCHAR column
      return new ScalarTypeJsonList.Varchar(getDocType(valueType));
    } else if (type.equals(Set.class)) {
      if (arrayTypeSetFactory != null) {
        return arrayTypeSetFactory.typeFor(valueType);
      }
      // fallback to JSON storage in VARCHAR column
      return new ScalarTypeJsonSet.Varchar(getDocType(valueType));
    }
    throw new IllegalStateException("Type [" + type + "] not supported for @DbArray");
  }

  @Override
  public ScalarType<?> getJsonScalarType(Class<?> type, int dbType, int dbLength, Type genericType) {

    if (type.equals(List.class)) {
      DocPropertyType docType = getDocType(genericType);
      if (isValueTypeSimple(genericType)) {
        return ScalarTypeJsonList.typeFor(postgres, dbType, docType);
      } else {
        return createJsonObjectMapperType(type, genericType, dbType, docType);
      }
    }

    if (type.equals(Set.class)) {
      DocPropertyType docType = getDocType(genericType);
      if (isValueTypeSimple(genericType)) {
        return ScalarTypeJsonSet.typeFor(postgres, dbType, docType);
      } else {
        return createJsonObjectMapperType(type, genericType, dbType, docType);
      }
    }

    if (type.equals(Map.class)) {
      if (isMapValueTypeObject(genericType)) {
        return ScalarTypeJsonMap.typeFor(postgres, dbType);
      } else {
        return createJsonObjectMapperType(type, genericType, dbType, DocPropertyType.OBJECT);
      }
    }

    if (type.equals(JsonNode.class)) {
      switch (dbType) {
        case Types.VARCHAR:
          return jsonNodeVarchar;
        case Types.BLOB:
          return jsonNodeBlob;
        case Types.CLOB:
          return jsonNodeClob;
        case DbPlatformType.JSONB:
          return jsonNodeJsonb;
        case DbPlatformType.JSON:
          return jsonNodeJson;
        default:
          return jsonNodeJson;
      }
    }

    return createJsonObjectMapperType(type, type, dbType, DocPropertyType.OBJECT);
  }

  private DocPropertyType getDocType(Type genericType) {
    if (genericType instanceof Class<?>) {
      ScalarType<?> found = typeMap.get(genericType);
      if (found != null) {
        return found.getDocType();
      }
    }
    return DocPropertyType.OBJECT;
  }

  /**
   * Return true if value parameter type of the map is Object.
   */
  private boolean isValueTypeSimple(Type genericType) {
    Type[] typeArgs = ((ParameterizedType) genericType).getActualTypeArguments();
    return String.class.equals(typeArgs[0]) || Long.class.equals(typeArgs[0]);
  }

  private Type getValueType(Type genericType) {
    Type[] typeArgs = ((ParameterizedType) genericType).getActualTypeArguments();
    return typeArgs[0];
  }

  /**
   * Return true if value parameter type of the map is Object.
   */
  private boolean isMapValueTypeObject(Type genericType) {
    Type[] typeArgs = ((ParameterizedType) genericType).getActualTypeArguments();
    return Object.class.equals(typeArgs[1]) || "?".equals(typeArgs[1].toString());
  }

  private ScalarType<?> createJsonObjectMapperType(Class<?> type, Type genericType, int dbType, DocPropertyType docType) {
    if (objectMapper == null) {
      throw new IllegalArgumentException("Type [" + type + "] unsupported for @DbJson mapping - Jackson ObjectMapper not present");
    }
    return ScalarTypeJsonObjectMapper.createTypeFor(postgres, type, (ObjectMapper) objectMapper, genericType, dbType, docType);
  }

  /**
   * Return a ScalarType for a given class.
   * <p>
   * Used for java.util.Date and java.util.Calendar which can be mapped to
   * different jdbcTypes in a single system.
   * </p>
   */
  @Override
  public ScalarType<?> getScalarType(Class<?> type, int jdbcType) {

    // File is a special Lob so check for that first
    if (File.class.equals(type)) {
      return fileType;
    }

    // check for Clob, LongVarchar etc ...
    // the reason being that String maps to multiple jdbc types
    // varchar, clob, longVarchar.
    ScalarType<?> scalarType = getLobTypes(jdbcType);
    if (scalarType != null) {
      // it is a specific Lob type...
      return scalarType;
    }

    scalarType = typeMap.get(type);
    if (scalarType != null) {
      if (jdbcType == 0 || scalarType.getJdbcType() == jdbcType) {
        // matching type
        return scalarType;
      }
    }
    // a util Date with jdbcType not matching server wide settings
    if (type.equals(java.util.Date.class)) {
      return extraTypeFactory.createUtilDate(jsonDateTime, jdbcType);
    }
    // a Calendar with jdbcType not matching server wide settings
    if (type.equals(java.util.Calendar.class)) {
      return extraTypeFactory.createCalendar(jsonDateTime, jdbcType);
    }

    throw new IllegalArgumentException("Unmatched ScalarType for " + type + " jdbcType:" + jdbcType);
  }

  /**
   * Return the types for the known lob types.
   * <p>
   * Kind of special case because these map multiple jdbc types to single Java
   * types - like String - Varchar, LongVarchar, Clob. For this reason I check
   * for the specific Lob types first before looking for a matching type.
   * </p>
   */
  private ScalarType<?> getLobTypes(int jdbcType) {

    return getScalarType(jdbcType);
  }

  /**
   * Convert the Object to the required datatype. The
   *
   * @param value      the Object value
   * @param toJdbcType the type as per java.sql.Types.
   */
  public Object convert(Object value, int toJdbcType) {
    if (value == null) {
      return null;
    }
    ScalarType<?> type = nativeMap.get(toJdbcType);
    if (type != null) {
      return type.toJdbcType(value);
    }
    return value;
  }

  boolean isIntegerType(String s) {
    if (isLeadingZeros(s)) {
      return false;
    }
    try {
      Integer.parseInt(s);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * Treat leading zeros as a non-integer for enum values.
   */
  private boolean isLeadingZeros(String s) {
    return s.length() > 1 && s.charAt(0) == '0';
  }

  /**
   * Create the Mapping of Enum fields to DB values using EnumValue annotations.
   * <p>
   * Return null if the EnumValue annotations are not present/used.
   * </p>
   */
  private ScalarType<?> createEnumScalarType2(Class<?> enumType) {

    boolean integerType = true;

    Map<String, String> nameValueMap = new HashMap<>();

    Field[] fields = enumType.getDeclaredFields();
    for (Field field : fields) {
      EnumValue enumValue = AnnotationUtil.findAnnotation(field, EnumValue.class);
      if (enumValue != null) {
        nameValueMap.put(field.getName(), enumValue.value());
        if (integerType && !isIntegerType(enumValue.value())) {
          // will treat the values as strings
          integerType = false;
        }
      }
    }
    if (nameValueMap.isEmpty()) {
      // Not using EnumValue here
      return null;
    }

    return createEnumScalarType(enumType, nameValueMap, integerType, 0);
  }

  /**
   * Create a ScalarType for an Enum that has additional mapping.
   * <p>
   * The reason for this is that often in a DB there will be short codes used
   * such as A,I,N rather than the ACTIVE, INACTIVE, NEW. So there really needs
   * to be a mapping from the nicely named enumeration values to the typically
   * much shorter codes used in the DB.
   * </p>
   */
  @Override
  public ScalarType<?> createEnumScalarType(Class<? extends Enum<?>> enumType) {

    Method[] methods = enumType.getMethods();
    for (Method method : methods) {
      DbEnumValue dbValue = AnnotationUtil.findAnnotation(method, DbEnumValue.class);
      if (dbValue != null) {
        boolean integerValues = DbEnumType.INTEGER == dbValue.storage();
        return createEnumScalarTypeDbValue(enumType, method, integerValues);
      }
    }


    // look for EnumValue annotations instead
    return createEnumScalarType2(enumType);
  }

  /**
   * Create the Mapping of Enum fields to DB values using EnumValue annotations.
   * <p>
   * Return null if the EnumValue annotations are not present/used.
   * </p>
   */
  private ScalarType<?> createEnumScalarTypeDbValue(Class<? extends Enum<?>> enumType, Method method, boolean integerType) {

    Map<String, String> nameValueMap = new HashMap<>();

    Enum<?>[] enumConstants = enumType.getEnumConstants();
    for (Enum<?> enumConstant : enumConstants) {
      try {
        Object value = method.invoke(enumConstant);
        nameValueMap.put(enumConstant.name(), value.toString());
      } catch (Exception e) {
        throw new IllegalArgumentException("Error trying to invoke DbEnumValue method on " + enumConstant, e);
      }
    }
    if (nameValueMap.isEmpty()) {
      // Not using EnumValue here
      return null;
    }

    return createEnumScalarType(enumType, nameValueMap, integerType, 0);
  }

  /**
   * Given the name value mapping and integer/string type and explicit DB column
   * length create the ScalarType for the Enum.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private ScalarType<?> createEnumScalarType(Class enumType, Map<String, String> nameValueMap, boolean integerType, int dbColumnLength) {

    EnumToDbValueMap<?> beanDbMap = EnumToDbValueMap.create(integerType);

    int maxValueLen = 0;

    for (Map.Entry<String, String> entry : nameValueMap.entrySet()) {
      String name = entry.getKey();
      String value = entry.getValue();

      maxValueLen = Math.max(maxValueLen, value.length());

      Object enumValue = Enum.valueOf(enumType, name.trim());
      beanDbMap.add(enumValue, value, name.trim());
    }

    if (dbColumnLength == 0 && !integerType) {
      dbColumnLength = maxValueLen;
    }

    return new ScalarTypeEnumWithMapping(beanDbMap, enumType, dbColumnLength);
  }

  /**
   * Automatically find any ScalarTypes by searching through the class path.
   * <p>
   * In avaje.properties define a list of packages in which ScalarTypes are
   * found. This will search for any class that implements the ScalarType
   * interface and register it with this TypeManager.
   * </p>
   */
  private void initialiseCustomScalarTypes(BootupClasses bootupClasses) {

    for (Class<? extends ScalarType<?>> cls : bootupClasses.getScalarTypes()) {
      try {

        ScalarType<?> scalarType;
        if (objectMapper == null) {
          scalarType = cls.newInstance();
        } else {
          try {
            // first try objectMapper constructor
            Constructor<? extends ScalarType<?>> constructor = cls.getConstructor(ObjectMapper.class);
            scalarType = constructor.newInstance((ObjectMapper) objectMapper);
          } catch (NoSuchMethodException e) {
            scalarType = cls.newInstance();
          }
        }

        addCustomType(scalarType);

      } catch (Exception e) {
        String msg = "Error loading ScalarType [" + cls.getName() + "]";
        logger.error(msg, e);
      }
    }
  }

  private void addCustomType(ScalarType<?> scalarType) {
    add(scalarType);
  }

  private Object initObjectMapper(ServerConfig serverConfig) {

    Object objectMapper = serverConfig.getObjectMapper();
    if (objectMapper == null) {
      objectMapper = new ObjectMapper();
      serverConfig.setObjectMapper(objectMapper);
    }
    return objectMapper;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void initialiseScalarConverters(BootupClasses bootupClasses) {

    List<Class<? extends ScalarTypeConverter<?, ?>>> foundTypes = bootupClasses.getScalarConverters();

    for (Class<? extends ScalarTypeConverter<?, ?>> foundType : foundTypes) {
      try {

        Class<?>[] paramTypes = TypeReflectHelper.getParams(foundType, ScalarTypeConverter.class);
        if (paramTypes.length != 2) {
          throw new IllegalStateException("Expected 2 generics paramtypes but got: " + Arrays.toString(paramTypes));
        }

        Class<?> logicalType = paramTypes[0];
        Class<?> persistType = paramTypes[1];

        ScalarType<?> wrappedType = getScalarType(persistType);
        if (wrappedType == null) {
          throw new IllegalStateException("Could not find ScalarType for: " + paramTypes[1]);
        }

        ScalarTypeConverter converter = foundType.newInstance();
        ScalarTypeWrapper stw = new ScalarTypeWrapper(logicalType, wrappedType, converter);
        logger.debug("Register ScalarTypeWrapper from {} -> {} using:{}", logicalType, persistType, foundType);
        add(stw);

      } catch (Exception e) {
        logger.error("Error registering ScalarTypeConverter [" + foundType.getName() + "]", e);
      }
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void initialiseAttributeConverters(BootupClasses bootupClasses) {

    List<Class<? extends AttributeConverter<?, ?>>> foundTypes = bootupClasses.getAttributeConverters();

    for (Class<? extends AttributeConverter<?, ?>> foundType : foundTypes) {
      try {

        Class<?>[] paramTypes = TypeReflectHelper.getParams(foundType, AttributeConverter.class);
        if (paramTypes.length != 2) {
          throw new IllegalStateException("Expected 2 generics paramtypes but got: " + Arrays.toString(paramTypes));
        }

        Class<?> logicalType = paramTypes[0];
        Class<?> persistType = paramTypes[1];

        ScalarType<?> wrappedType = getScalarType(persistType);
        if (wrappedType == null) {
          throw new IllegalStateException("Could not find ScalarType for: " + paramTypes[1]);
        }

        AttributeConverter converter = foundType.newInstance();
        ScalarTypeWrapper stw = new ScalarTypeWrapper(logicalType, wrappedType, new AttributeConverterAdapter(converter));
        logger.debug("Register ScalarTypeWrapper from {} -> {} using:{}", logicalType, persistType, foundType);
        add(stw);

      } catch (Exception e) {
        logger.error("Error registering AttributeConverter [" + foundType.getName() + "]", e);
      }
    }
  }

  /**
   * Add support for Jackson's JsonNode mapping to Clob, Blob, Varchar, JSON and JSONB.
   */
  private void initialiseJacksonTypes(ServerConfig config) {

    if (objectMapper != null) {

      logger.trace("Registering JsonNode type support");

      ObjectMapper mapper = (ObjectMapper) objectMapper;
      jsonNodeClob = new ScalarTypeJsonNode.Clob(mapper);
      jsonNodeBlob = new ScalarTypeJsonNode.Blob(mapper);
      jsonNodeVarchar = new ScalarTypeJsonNode.Varchar(mapper);
      jsonNodeJson = jsonNodeClob;  // Default for non-Postgres databases
      jsonNodeJsonb = jsonNodeClob; // Default for non-Postgres databases

      if (isPostgres(config.getDatabasePlatform())) {
        jsonNodeJson = new ScalarTypeJsonNodePostgres.JSON(mapper);
        jsonNodeJsonb = new ScalarTypeJsonNodePostgres.JSONB(mapper);
      }

      // add as default mapping for JsonNode (when not annotated with @DbJson etc)
      typeMap.put(JsonNode.class, jsonNodeJson);
    }
  }

  private void initialiseJavaTimeTypes(JsonConfig.DateTime mode, ServerConfig config) {

    if (java7Present) {
      typeMap.put(java.nio.file.Path.class, new ScalarTypePath());
    }

    if (config.getClassLoadConfig().isJavaTimePresent()) {
      logger.debug("Registering java.time data types");
      typeMap.put(java.time.LocalDate.class, new ScalarTypeLocalDate());
      typeMap.put(java.time.LocalDateTime.class, new ScalarTypeLocalDateTime(mode));
      typeMap.put(OffsetDateTime.class, new ScalarTypeOffsetDateTime(mode));
      typeMap.put(ZonedDateTime.class, new ScalarTypeZonedDateTime(mode));
      typeMap.put(Instant.class, new ScalarTypeInstant(mode));

      typeMap.put(DayOfWeek.class, new ScalarTypeDayOfWeek());
      typeMap.put(Month.class, new ScalarTypeMonth());
      typeMap.put(Year.class, new ScalarTypeYear());
      typeMap.put(YearMonth.class, new ScalarTypeYearMonthDate());
      typeMap.put(MonthDay.class, new ScalarTypeMonthDay());
      typeMap.put(OffsetTime.class, new ScalarTypeOffsetTime());
      typeMap.put(ZoneId.class, new ScalarTypeZoneId());
      typeMap.put(ZoneOffset.class, new ScalarTypeZoneOffset());

      boolean localTimeNanos = config.isLocalTimeWithNanos();
      typeMap.put(java.time.LocalTime.class, (localTimeNanos) ? new ScalarTypeLocalTimeWithNanos() : new ScalarTypeLocalTime());

      boolean durationNanos = config.isDurationWithNanos();
      typeMap.put(Duration.class, (durationNanos) ? new ScalarTypeDurationWithNanos() : new ScalarTypeDuration());
    }
  }

  /**
   * Detect if Joda classes are in the classpath and if so register the Joda data types.
   */
  @SuppressWarnings("deprecation")
  private void initialiseJodaTypes(JsonConfig.DateTime mode, ServerConfig config) {

    // detect if Joda classes are in the classpath
    if (config.getClassLoadConfig().isJodaTimePresent()) {
      // Joda classes are in the classpath so register the types
      logger.debug("Registering Joda data types");
      typeMap.put(LocalDateTime.class, new ScalarTypeJodaLocalDateTime(mode));
      typeMap.put(DateTime.class, new ScalarTypeJodaDateTime(mode));
      typeMap.put(LocalDate.class, new ScalarTypeJodaLocalDate());
      typeMap.put(org.joda.time.DateMidnight.class, new ScalarTypeJodaDateMidnight());

      String jodaLocalTimeMode = config.getJodaLocalTimeMode();
      if ("normal".equalsIgnoreCase(jodaLocalTimeMode)) {
        // use the expected/normal local time zone
        typeMap.put(LocalTime.class, new ScalarTypeJodaLocalTime());
        logger.debug("registered ScalarTypeJodaLocalTime");
      } else if ("utc".equalsIgnoreCase(jodaLocalTimeMode)) {
        // use the old UTC based
        typeMap.put(LocalTime.class, new ScalarTypeJodaLocalTimeUTC());
        logger.debug("registered ScalarTypeJodaLocalTimeUTC");
      }
    }
  }

  /**
   * Register all the standard types supported. This is the standard JDBC types
   * plus some other common types such as java.util.Date and java.util.Calendar.
   */
  private void initialiseStandard(JsonConfig.DateTime mode, ServerConfig config) {

    DatabasePlatform databasePlatform = config.getDatabasePlatform();
    int platformClobType = databasePlatform.getClobDbType();
    int platformBlobType = databasePlatform.getBlobDbType();

    nativeMap.put(DbPlatformType.HSTORE, hstoreType);

    ScalarType<?> utilDateType = extraTypeFactory.createUtilDate(mode);
    typeMap.put(java.util.Date.class, utilDateType);

    ScalarType<?> calType = extraTypeFactory.createCalendar(mode);
    typeMap.put(Calendar.class, calType);

    ScalarType<?> mathBigIntType = extraTypeFactory.createMathBigInteger();
    typeMap.put(BigInteger.class, mathBigIntType);

    ScalarTypeBool booleanType = extraTypeFactory.createBoolean();
    typeMap.put(Boolean.class, booleanType);
    typeMap.put(boolean.class, booleanType);

    // register the boolean literals to the platform for DDL default values
    databasePlatform.setDbTrueLiteral(booleanType.getDbTrueLiteral());
    databasePlatform.setDbFalseLiteral(booleanType.getDbFalseLiteral());

    // always register Types.BOOLEAN to our boolean type
    nativeMap.put(Types.BOOLEAN, booleanType);
    if (booleanType.getJdbcType() == Types.BIT) {
      // for MapBeans ... BIT types are assumed to be booleans
      nativeMap.put(Types.BIT, booleanType);
    }

    ServerConfig.DbUuid dbUuid = config.getDbTypeConfig().getDbUuid();

    if (offlineMigrationGeneration || (databasePlatform.isNativeUuidType() && dbUuid.useNativeType())) {
      typeMap.put(UUID.class, new ScalarTypeUUIDNative());
    } else {
      // Store UUID as binary(16) or varchar(40)
      ScalarType<?> uuidType = dbUuid.useBinary() ? new ScalarTypeUUIDBinary() : new ScalarTypeUUIDVarchar();
      typeMap.put(UUID.class, uuidType);
    }

    typeMap.put(File.class, fileType);
    typeMap.put(InetAddress.class, inetAddressType);
    typeMap.put(Locale.class, localeType);
    typeMap.put(Currency.class, currencyType);
    typeMap.put(TimeZone.class, timeZoneType);
    typeMap.put(URL.class, urlType);
    typeMap.put(URI.class, uriType);

    // String types
    typeMap.put(char[].class, charArrayType);
    typeMap.put(char.class, charType);
    typeMap.put(String.class, stringType);
    nativeMap.put(Types.VARCHAR, stringType);
    nativeMap.put(Types.CHAR, stringType);
    nativeMap.put(Types.LONGVARCHAR, longVarcharType);

    // Class<?>
    typeMap.put(Class.class, classType);

    if (platformClobType == Types.CLOB) {
      nativeMap.put(Types.CLOB, clobType);
    } else {
      // for Postgres Clobs handled by Varchar ScalarType...
      ScalarType<?> platClobScalarType = nativeMap.get(platformClobType);
      if (platClobScalarType == null) {
        throw new IllegalArgumentException("Type for dbPlatform clobType [" + clobType + "] not found.");
      }
      nativeMap.put(Types.CLOB, platClobScalarType);
    }

    // Binary type
    typeMap.put(byte[].class, varbinaryType);
    nativeMap.put(Types.BINARY, binaryType);
    nativeMap.put(Types.VARBINARY, varbinaryType);
    nativeMap.put(Types.LONGVARBINARY, longVarbinaryType);

    if (platformBlobType == Types.BLOB) {
      nativeMap.put(Types.BLOB, blobType);
    } else {
      // for Postgres Blobs handled by LongVarbinary ScalarType...
      ScalarType<?> platBlobScalarType = nativeMap.get(platformBlobType);
      if (platBlobScalarType == null) {
        throw new IllegalArgumentException("Type for dbPlatform blobType [" + blobType + "] not found.");
      }
      nativeMap.put(Types.BLOB, platBlobScalarType);
    }

    // Number types
    typeMap.put(Byte.class, byteType);
    typeMap.put(byte.class, byteType);
    nativeMap.put(Types.TINYINT, byteType);

    typeMap.put(Short.class, shortType);
    typeMap.put(short.class, shortType);
    nativeMap.put(Types.SMALLINT, shortType);

    typeMap.put(Integer.class, integerType);
    typeMap.put(int.class, integerType);
    nativeMap.put(Types.INTEGER, integerType);

    typeMap.put(Long.class, longType);
    typeMap.put(long.class, longType);
    nativeMap.put(Types.BIGINT, longType);

    typeMap.put(Double.class, doubleType);
    typeMap.put(double.class, doubleType);
    nativeMap.put(Types.FLOAT, doubleType);// no this is not a bug
    nativeMap.put(Types.DOUBLE, doubleType);

    typeMap.put(Float.class, floatType);
    typeMap.put(float.class, floatType);
    nativeMap.put(Types.REAL, floatType);// no this is not a bug

    typeMap.put(BigDecimal.class, bigDecimalType);
    nativeMap.put(Types.DECIMAL, bigDecimalType);
    nativeMap.put(Types.NUMERIC, bigDecimalType);

    // Temporal types
    typeMap.put(Time.class, timeType);
    nativeMap.put(Types.TIME, timeType);
    typeMap.put(Date.class, dateType);
    nativeMap.put(Types.DATE, dateType);

    ScalarType<?> timestampType = new ScalarTypeTimestamp(mode);
    typeMap.put(Timestamp.class, timestampType);
    nativeMap.put(Types.TIMESTAMP, timestampType);
  }

}
