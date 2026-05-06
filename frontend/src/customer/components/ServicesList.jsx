import { ServiceCard } from './ServiceCard'
import { services } from './servicesData'

/**
 * Contains the list of services for a given category.
 */
export const ServicesList = ({ category, onBookService, onRemoveService, cartItems }) => {
  return (
    <div className="mx-12">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {services[category].map((service) => (
          <ServiceCard
            key={service.id}
            service={service}
            onBook={onBookService}
            onRemove={onRemoveService}
            isInCart={cartItems.has(service.id)}
          />
        ))}
      </div>
    </div>
  )
}