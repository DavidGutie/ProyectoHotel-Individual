using System.Text.Json.Serialization;

namespace HOTELINTERFAZ.Models
{
    public class Amenity
    {
        [JsonPropertyName("_id")]
        public string Id { get; set; } = "";

        [JsonPropertyName("name")]
        public string Name { get; set; } = "";

        [JsonPropertyName("icon")]
        public string Icon { get; set; } = "";

        [JsonPropertyName("category")]
        public string Category { get; set; } = "";

        [JsonIgnore]
        public string DisplayName => string.IsNullOrWhiteSpace(Icon)
            ? Name
            : $"{Icon} {Name}";
    }
}
