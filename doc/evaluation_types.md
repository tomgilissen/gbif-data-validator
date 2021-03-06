Evaluation Type represents what the evaluation discovered which is not always an issue.

### Resource Integrity
|Key            |Description    |
| ------------- | ------------- |
DWCA_UNREADABLE | Impossible to read or extract the DarwinCore Archive
DWCA_META_XML_NOT_FOUND | Impossible to find the meta.xml file
DWCA_META_XML_SCHEMA | meta.xml file can not be validated against its schema
RECORD_IDENTIFIER_NOT_FOUND | No record identifier was found within the file
CORE_ROWTYPE_UNDETERMINED | No rowType could be found or determined

### Resource Structure
|Key            |Description    |
| ------------- | ------------- |
|EML_NOT_FOUND| No EML document was found
|EML_GBIF_SCHEMA| The EML document does not validate against the schema
|UNKNOWN_ROWTYPE| The rowType used for the core/extension is unknown
|REQUIRED_TERM_MISSING| A required term for the core/extension is missing
|UNKNOWN_TERM| A term used in the core/extension definition is unknown
|RECORD_NOT_UNIQUELY_IDENTIFIED| The record in the core file is not uniquely identified
|RECORD_REFERENTIAL_INTEGRITY_VIOLATION| The record in an extension does not link to an existing core record
|COLUMN_MISMATCH| The number of column used on the line does not match the expected number

### Metadata Content
|Key            |Description    |
| ------------- | ------------- |
|TITLE_MISSING_OR_TOO_SHORT| The title of the dataset is missing or too short
|DESCRIPTION_MISSING_OR_TOO_SHORT| The description of the dataset is missing or too short
|LICENSE_MISSING_OR_UNKNOWN| The license can not be parsed, is not supported by GBIF or is simply missing
|RESOURCE_CREATOR_MISSING_OR_INCOMPLETE| The resource creator is missing or is incomplete

### Occurrence Interpretation Based

|Key            |Description    |
| ------------- | ------------- |
|ZERO_COORDINATE|
|COORDINATE_OUT_OF_RANGE|
|COORDINATE_INVALID|
|COORDINATE_ROUNDED|
|GEODETIC_DATUM_INVALID|
|GEODETIC_DATUM_ASSUMED_WGS84|
|COORDINATE_REPROJECTED|
|COORDINATE_REPROJECTION_FAILED|
|COORDINATE_REPROJECTION_SUSPICIOUS|
|COORDINATE_PRECISION_INVALID|
|COORDINATE_UNCERTAINTY_METERS_INVALID|
|COUNTRY_COORDINATE_MISMATCH| Geographic coordinates fall outside the area defined by the referenced boundary of the country [1]
|COUNTRY_MISMATCH|
|COUNTRY_INVALID|
|COUNTRY_DERIVED_FROM_COORDINATES|
|CONTINENT_COUNTRY_MISMATCH|
|CONTINENT_INVALID|
|CONTINENT_DERIVED_FROM_COORDINATES|
|PRESUMED_SWAPPED_COORDINATE|
|PRESUMED_NEGATED_LONGITUDE|
|PRESUMED_NEGATED_LATITUDE|
|RECORDED_DATE_MISMATCH| The date represented by `eventDate` and the atomic version (`year`, `month`, `day`) does not represent the same date. Warning: https://github.com/gbif/parsers/issues/8
|RECORDED_DATE_INVALID|
|RECORDED_DATE_UNLIKELY|
|TAXON_MATCH_FUZZY|
|TAXON_MATCH_HIGHERRANK|
|TAXON_MATCH_NONE|
|DEPTH_NOT_METRIC|
|DEPTH_UNLIKELY|
|DEPTH_MIN_MAX_SWAPPED|
|DEPTH_NON_NUMERIC|
|ELEVATION_UNLIKELY|
|ELEVATION_MIN_MAX_SWAPPED|
|ELEVATION_NOT_METRIC|
|ELEVATION_NON_NUMERIC|
|MODIFIED_DATE_INVALID|
|MODIFIED_DATE_UNLIKELY|
|IDENTIFIED_DATE_UNLIKELY| The value of `dateIdentified` unlikely. e.g date in the future. Warning: https://github.com/gbif/parsers/issues/9
|IDENTIFIED_DATE_INVALID| The value of `dateIdentified` invalid. Can not be turned into a valid date.
|BASIS_OF_RECORD_INVALID| The value of `basisOfRecord` is either missing, or invalid. Value must match [Darwin Core Type Vocabulary](http://rs.gbif.org/vocabulary/dwc/basis_of_record.xml).
|TYPE_STATUS_INVALID|
|MULTIMEDIA_DATE_INVALID|
|MULTIMEDIA_URI_INVALID|
|REFERENCES_URI_INVALID|
|INTERPRETATION_ERROR|
|INDIVIDUAL_COUNT_INVALID|

### ChecklistBank Interpretation based
PARENT_NAME_USAGE_ID_INVALID
ACCEPTED_NAME_USAGE_ID_INVALID
ORIGINAL_NAME_USAGE_ID_INVALID
ACCEPTED_NAME_MISSING
RANK_INVALID
NOMENCLATURAL_STATUS_INVALID
TAXONOMIC_STATUS_INVALID
SCIENTIFIC_NAME_ASSEMBLED
CHAINED_SYNOYM
BASIONYM_AUTHOR_MISMATCH
TAXONOMIC_STATUS_MISMATCH
PARENT_CYCLE
CLASSIFICATION_RANK_ORDER_INVALID
CLASSIFICATION_NOT_APPLIED
VERNACULAR_NAME_INVALID
DESCRIPTION_INVALID
DISTRIBUTION_INVALID
SPECIES_PROFILE_INVALID
MULTIMEDIA_INVALID
BIB_REFERENCE_INVALID
ALT_IDENTIFIER_INVALID
BACKBONE_MATCH_NONE
// BACKBONE_MATCH_FUZZY id deprecated
ACCEPTED_NAME_NOT_UNIQUE
PARENT_NAME_NOT_UNIQUE
ORIGINAL_NAME_NOT_UNIQUE
RELATIONSHIP_MISSING
ORIGINAL_NAME_DERIVED
CONFLICTING_BASIONYM_COMBINATION
NO_SPECIES
NAME_PARENT_MISMATCH
ORTHOGRAPHIC_VARIANT
HOMONYM
PUBLISHED_BEFORE_GENUS

[1] Mostly based on [Exclusive economic zone (EEZ)](https://en.wikipedia.org/wiki/Exclusive_economic_zone)
