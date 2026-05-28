package fp.practices.ocularis_mobile.data.model

data class DashboardVisualContent(
    val about: AboutInfo,
    val contact: ContactInfo,
    val promotions: List<Promotion>,
    val curiosities: List<Curiosity>,
    val financingPlans: List<String>,
    val operations: List<String>,
    val tips: List<String>
) {
    companion object {
        fun default(): DashboardVisualContent {
            return DashboardVisualContent(
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
                        imageUrl = "https://images.pexels.com/photos/3845128/pexels-photo-3845128.jpeg?auto=compress&w=400&q=80",
                        details = "Desde 950€ por ojo. Consulta gratuita. Financiación disponible."
                    ),
                    Promotion(
                        title = "Ojo Seco",
                        description = "Tratamientos de última generación y terapias especializadas.",
                        imageUrl = "https://images.pexels.com/photos/3845734/pexels-photo-3845734.jpeg?auto=compress&w=400&q=80",
                        details = "Consulta inicial 40€. Pack tratamiento desde 120€."
                    ),
                    Promotion(
                        title = "Presbicia",
                        description = "Mejora tu visión de cerca con nuestros lentes progresivos de última generación y opciones de lentes de contacto premium.",
                        imageUrl = "https://images.pexels.com/photos/3845761/pexels-photo-3845761.jpeg?auto=compress&w=400&q=80",
                        details = "Lentes desde 180€. Consulta gratuita."
                    ),
                    Promotion(
                        title = "Cataratas",
                        description = "Recuperación visual con cirugía de facoemulsificación de precisión e implante de lentes intraoculares multifocales.",
                        imageUrl = "https://images.pexels.com/photos/3845802/pexels-photo-3845802.jpeg?auto=compress&w=400&q=80",
                        details = "Cirugía desde 1.200€ por ojo. Financiación disponible."
                    )
                ),
                curiosities = listOf(
                    Curiosity(
                        text = "Las lágrimas no solo lubrican, también contienen enzimas que protegen contra infecciones y nutren la córnea.",
                        imageUrl = "https://images.pexels.com/photos/3845820/pexels-photo-3845820.jpeg?auto=compress&w=400&q=80"
                    ),
                    Curiosity(
                        text = "El músculo del párpado es el más rápido del cuerpo: el parpadeo es tan rápido que dura solo 1/10 de segundo.",
                        imageUrl = "https://images.pexels.com/photos/3845734/pexels-photo-3845734.jpeg?auto=compress&w=400&q=80"
                    ),
                    Curiosity(
                        text = "El ojo humano puede distinguir alrededor de 10 millones de colores diferentes.",
                        imageUrl = "https://images.pexels.com/photos/3845761/pexels-photo-3845761.jpeg?auto=compress&w=400&q=80"
                    ),
                    Curiosity(
                        text = "La córnea es el único tejido del cuerpo humano que no contiene vasos sanguíneos.",
                        imageUrl = "https://images.pexels.com/photos/3845802/pexels-photo-3845802.jpeg?auto=compress&w=400&q=80"
                    ),
                    Curiosity(
                        text = "Los ojos parpadean aproximadamente 15-20 veces por minuto, lo que ayuda a mantenerlos limpios y húmedos.",
                        imageUrl = "https://images.pexels.com/photos/3845128/pexels-photo-3845128.jpeg?auto=compress&w=400&q=80"
                    ),
                    Curiosity(
                        text = "El ojo humano puede enfocar en tan solo 2 milisegundos, más rápido que una cámara profesional.",
                        imageUrl = "https://images.pexels.com/photos/3845850/pexels-photo-3845850.jpeg?auto=compress&w=400&q=80"
                    ),
                    Curiosity(
                        text = "Las lágrimas emocionales contienen hormonas y proteínas diferentes a las lágrimas basales.",
                        imageUrl = "https://images.pexels.com/photos/1130626/pexels-photo-1130626.jpeg?auto=compress&w=400&q=80"
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

data class ContactInfo(
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val hours: String
)

data class AboutInfo(
    val title: String,
    val description: String,
    val ctaLabel: String
)

data class Promotion(
    val title: String,
    val description: String,
    val imageUrl: String,
    val details: String
)

data class Curiosity(
    val text: String,
    val imageUrl: String
)

