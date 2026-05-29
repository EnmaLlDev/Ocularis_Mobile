package fp.practices.ocularis_mobile.data.model

import fp.practices.ocularis_mobile.R

/**
 * Modelo del dashboard con contenido visual de la clínica.
 */
data class Dashboard(
    val patients: Int = 0,
    val doctors: Int = 0,
    val appointments: Int = 0,
    val about: AboutInfo,
    val contact: ContactInfo,
    val promotions: List<Promotion>,
    val curiosities: List<Curiosity>,
    val financingPlans: List<String>,
    val operations: List<String>,
    val tips: List<String>
) {
    companion object {
        fun default(): Dashboard {
            return Dashboard(
                about = AboutInfo(
                    title = "SOBRE OCULARIS",
                    description = "Líderes en tratamientos oftalmologícos, Clínica Ocularis se dedica a proporcionar cuidado visual excepcional utilizando la tecnología de vanguardia y un equipo de especialistas dedicados. Nuestro compromiso es su visión, brindando atención personalizada para toda su familia.",
                    ctaLabel = "Más Información"
                ),
                contact = ContactInfo(
                    name = "Clínica Ocularis",
                    phone = "+34 900 123 456",
                    email = "contacto@ocularis.com",
                    address = "Av. Salud Visual 123, Madrid",
                    hours = "Lun-Vie 9:00-19:00"
                ),
                promotions = listOf(
                    Promotion(
                        title = "Miopía",
                        description = "Soluciones quirúrgicas avanzadas para corregir la visión de lejos, incluyendo LASIK y PRK en Clínica Ocularis.",
                        imageRes = R.drawable.miopia_review,
                        details = "Desde 950€ por ojo. Consulta gratuita. Financiación disponible."
                    ),
                    Promotion(
                        title = "Ojo Seco",
                        description = "Tratamientos de última generación y terapias especializadas.",
                        imageRes = R.drawable.eyes_review,
                        details = "Consulta inicial 40€. Pack tratamiento desde 120€."
                    ),
                    Promotion(
                        title = "Presbicia",
                        description = "Mejora tu visión de cerca con nuestros lentes progresivos de última generación y opciones de lentes de contacto premium.",
                        imageRes = R.drawable.doctor_review,
                        details = "Lentes desde 180€. Consulta gratuita."
                    ),
                    Promotion(
                        title = "Cataratas",
                        description = "Recuperación visual con cirugía de facoemulsificación de precisión e implante de lentes intraoculares multifocales.",
                        imageRes = R.drawable.cataratas_review,
                        details = "Cirugía desde 1.200€ por ojo. Financiación disponible."
                    )
                ),
                curiosities = listOf(
                    Curiosity(
                        title = "Lágrimas protectoras",
                        description = "Las lágrimas no solo lubrican, también contienen enzimas que protegen contra infecciones y nutren la córnea.",
                        imageRes = R.drawable.teardrop_data
                    ),
                    Curiosity(
                        title = "Parpadeo ultrarrápido",
                        description = "El músculo del párpado es el más rápido del cuerpo: el parpadeo dura solo 1/10 de segundo.",
                        imageRes = R.drawable.mucles_eyes_data
                    ),
                    Curiosity(
                        title = "Millones de colores",
                        description = "El ojo humano puede distinguir alrededor de 10 millones de colores diferentes.",
                        imageRes = R.drawable.colors_data
                    ),
                    Curiosity(
                        title = "Córnea sin vasos sanguíneos",
                        description = "La córnea es el único tejido del cuerpo humano que no contiene vasos sanguíneos.",
                        imageRes = R.drawable.cornea_data
                    ),
                    Curiosity(
                        title = "Parpadeo constante",
                        description = "Los ojos parpadean aproximadamente 15-20 veces por minuto, lo que ayuda a mantenerlos limpios.",
                        imageRes = R.drawable.parpadeo_data
                    )
                ),
                financingPlans = listOf(
                    "Plan Esencial: consultas y revisiones básicas en 3 cuotas sin intereses.",
                    "Plan Premium: cirugía + controles postoperatorios hasta 12 meses financiados.",
                    "Plan Familiar: descuentos por grupo y pagos fraccionados hasta 6 cuotas."
                ),
                operations = listOf(
                    "Cirugía refractiva (LASIK/PRK)",
                    "Cirugía de cataratas",
                    "Implante de lentes intraoculares",
                    "Cross-linking corneal",
                    "Tratamiento de ojo seco avanzado",
                    "Control de miopía en niños y adolescentes"
                ),
                tips = listOf(
                    "Revisa tus citas programadas con frecuencia.",
                    "Si necesitas cambiar una cita, contacta a la clínica.",
                    "Mantén tus datos de contacto actualizados."
                )
            )
        }
    }
}

/**
 * Información de contacto de la clínica.
 */
data class ContactInfo(
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val hours: String
)

/**
 * Información sobre la clínica.
 */
data class AboutInfo(
    val title: String,
    val description: String,
    val ctaLabel: String
)

/**
 * Promoción o tratamiento ofrecido por la clínica.
 */
data class Promotion(
    val title: String,
    val description: String,
    val imageRes: Int,
    val details: String
)

/**
 * Curiosidad oftalmológica para mostrar en el dashboard.
 */
data class Curiosity(
    val title: String,
    val description: String,
    val imageRes: Int
)
