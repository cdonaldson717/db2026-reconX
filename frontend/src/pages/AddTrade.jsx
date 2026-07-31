import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { withAuth } from '@components/withAuth.jsx';
import { api } from '@services/apiService.js';

const requiredNumber = (label) => yup
  .number()
  .transform((value, originalValue) => originalValue === '' ? undefined : value)
  .typeError(`${label} must be a number`)
  .required(`${label} is required`);

export const tradeSchema = yup.object({
  tradeRef: yup
    .string()
    .required('Trade reference is required')
    .matches(/^[A-Z]{3}-\d{8}-\d{4}$/, 'Use format AAA-YYYYMMDD-NNNN'),
  instrumentId: requiredNumber('Instrument ID')
    .integer('Instrument ID must be an integer')
    .positive('Instrument ID must be positive'),
  counterpartyId: requiredNumber('Counterparty ID')
    .integer('Counterparty ID must be an integer')
    .positive('Counterparty ID must be positive'),
  assetClass: yup
    .string()
    .oneOf(['EQUITY', 'FX', 'BOND', 'DERIVATIVE'], 'Select a valid asset class')
    .required('Asset class is required'),
  side: yup
    .string()
    .oneOf(['BUY', 'SELL'], 'Select BUY or SELL')
    .required('Side is required'),
  quantity: requiredNumber('Quantity').positive('Quantity must be positive'),
  price: requiredNumber('Price').positive('Price must be positive'),
  tradeDate: yup
    .string()
    .required('Trade date is required')
    .matches(/^\d{4}-\d{2}-\d{2}$/, 'Trade date must be YYYY-MM-DD')
    .test('not-in-future', 'Trade date cannot be in the future', (value) => {
      if (!value) return true;
      return value <= new Date().toISOString().slice(0, 10);
    }),
});

const defaultValues = {
  tradeRef: '',
  instrumentId: '',
  counterpartyId: '',
  assetClass: '',
  side: '',
  quantity: '',
  price: '',
  tradeDate: '',
};

function FieldError({ error }) {
  return error ? <p className="form-error" role="alert">{error.message}</p> : null;
}

export function AddTrade() {
  const [serverError, setServerError] = useState('');
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: yupResolver(tradeSchema),
    mode: 'onBlur',
    defaultValues,
  });

  async function onSubmit(values) {
    setServerError('');
    try {
      await api.createTrade(values);
      reset(defaultValues);
    } catch (error) {
      setServerError(error.message || 'Unable to create trade');
    }
  }

  return (
    <section>
      <h2>Add trade</h2>
      <form onSubmit={handleSubmit(onSubmit)} className="trade-form" noValidate>
        <label>
          Trade reference
          <input {...register('tradeRef')} placeholder="EQU-20260603-0001" />
        </label>
        <FieldError error={errors.tradeRef} />

        <label>
          Instrument ID
          <input type="number" {...register('instrumentId')} />
        </label>
        <FieldError error={errors.instrumentId} />

        <label>
          Counterparty ID
          <input type="number" {...register('counterpartyId')} />
        </label>
        <FieldError error={errors.counterpartyId} />

        <label>
          Asset class
          <select {...register('assetClass')}>
            <option value="">Select asset class</option>
            <option value="EQUITY">EQUITY</option>
            <option value="FX">FX</option>
            <option value="BOND">BOND</option>
            <option value="DERIVATIVE">DERIVATIVE</option>
          </select>
        </label>
        <FieldError error={errors.assetClass} />

        <label>
          Side
          <select {...register('side')}>
            <option value="">Select side</option>
            <option value="BUY">BUY</option>
            <option value="SELL">SELL</option>
          </select>
        </label>
        <FieldError error={errors.side} />

        <label>
          Quantity
          <input type="number" step="0.0001" {...register('quantity')} />
        </label>
        <FieldError error={errors.quantity} />

        <label>
          Price
          <input type="number" step="0.0001" {...register('price')} />
        </label>
        <FieldError error={errors.price} />

        <label>
          Trade date
          <input type="date" max={new Date().toISOString().slice(0, 10)} {...register('tradeDate')} />
        </label>
        <FieldError error={errors.tradeDate} />

        {serverError && <p className="form-error" role="alert">{serverError}</p>}

        <button disabled={isSubmitting} type="submit">
          {isSubmitting ? 'Submitting…' : 'Submit'}
        </button>
      </form>
    </section>
  );
}

export default withAuth(AddTrade);
