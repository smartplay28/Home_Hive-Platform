/**
 * Contains the ServiceCard component
 */
export const ServiceCard = ({ service, onBook, onRemove, isInCart }) => {
    return (
        <div className="bg-white shadow-md rounded-lg p-4 hover:shadow-lg transition-shadow flex flex-col w-full max-w-xl">
            <div className="w-[70%] aspect-square mb-4 mx-auto flex justify-center">
                <img
                    src={service.image}
                    alt={service.name}
                    className="w-full h-full object-cover rounded-lg"
                />
            </div>
            <h3 className="text-xl font-semibold mb-2">{service.name}</h3>
            <p className="text-gray-600 mb-2">{service.description}</p>

            <div className="mb-4">
                <div className="flex items-center text-sm text-gray-500 mb-2">
                    <svg
                        className="w-5 h-5 mr-2"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                    >
                        <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth="2"
                            d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                        />
                    </svg>
                    Duration: {service.duration}
                </div>

                <div className="text-sm text-gray-500">
                    <p className="font-medium mb-1">Includes:</p>
                    <ul className="list-disc pl-5">
                        {service.includes.map((item, index) => (
                            <li key={index}>{item}</li>
                        ))}
                    </ul>
                </div>
            </div>

            <div className="flex justify-between items-center mt-auto">
                <p className="text-blue-600 font-bold">{service.price}</p>
                {isInCart ? (
                    <button
                        onClick={() => onRemove(service.id)}
                        className="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 transition-colors"
                    >
                        Remove from Cart
                    </button>
                ) : (
                    <button
                        onClick={() => onBook(service.id)}
                        className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
                    >
                        Add To Cart
                    </button>
                )}
            </div>
        </div>
    );
};