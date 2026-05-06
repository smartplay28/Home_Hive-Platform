/**
 * Vitest global test setup.
 * Runs before every test file.
 *
 * Sets up @testing-library/jest-dom matchers so we can use:
 *   expect(el).toBeInTheDocument()
 *   expect(el).toHaveTextContent('foo')
 *   expect(el).toBeDisabled()
 *   etc.
 */
import '@testing-library/jest-dom';
